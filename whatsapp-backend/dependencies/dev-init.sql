create table databasechangeloglock
(
    id          int,
    locked      boolean,
    lockgranted timestamp,
    lockedby    varchar(255)
);
