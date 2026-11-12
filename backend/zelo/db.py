from contextlib import contextmanager
from pathlib import Path

from sqlalchemy import create_engine, event, text
from sqlalchemy.orm import DeclarativeBase, sessionmaker

from .config import settings


class Base(DeclarativeBase):
    pass


url = settings().database_url
if not url.startswith("sqlite:///"):
    raise ValueError("Only SQLite is supported by this pilot")
if url != "sqlite:///:memory:":
    Path(url.removeprefix("sqlite:///")).parent.mkdir(parents=True, exist_ok=True)
engine = create_engine(url, connect_args={"check_same_thread": False, "timeout": 30})


@event.listens_for(engine, "connect")
def sqlite_config(connection, _):
    connection.execute("PRAGMA foreign_keys=ON")
    connection.execute("PRAGMA journal_mode=WAL")
    connection.execute("PRAGMA busy_timeout=30000")


Session = sessionmaker(engine, expire_on_commit=False)


@contextmanager
def transaction():
    # SQLite ignores SELECT FOR UPDATE: reserve the write lock across processes.
    with Session() as session:
        session.execute(text("BEGIN IMMEDIATE"))
        try:
            yield session
            session.commit()
        except BaseException:
            session.rollback()
            raise
