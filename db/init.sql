CREATE TABLE IF NOT EXISTS "public"."memes" (
    "id" serial,
    "title" text NOT NULL,
    "added_at" timestamp NOT NULL,
    "points" integer NOT NULL,
    "author" integer NOT NULL,
    PRIMARY KEY ("id")
);
CREATE TABLE IF NOT EXISTS "public"."users" (
    "id" serial,
    "login" text NOT NULL,
    "password" text NOT NULL,
    "name" text,
    "surname" timestamp NOT NULL,
    "avatar" text,
    PRIMARY KEY ("id")
);
CREATE TABLE IF NOT EXISTS "public"."comments" (
    "id" serial,
    "meme_id" integer NOT NULL,
    "text" text NOT NULL,
    "points" integer NOT NULL,
    "added_at" timestamp NOT NULL,
    "author" integer not null,
    PRIMARY KEY ("id")
);
CREATE TABLE IF NOT EXISTS "public"."content" (
    "id" serial,
    "meme_id" integer NOT NULL,
    "content_type" text NOT NULL,
    "content" integer NOT NULL,
    "number" integer NOT NULL,
    PRIMARY KEY ("id")
);