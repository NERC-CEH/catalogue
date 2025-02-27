--
-- PostgreSQL database dump
--

-- Dumped from database version 14.1
-- Dumped by pg_dump version 14.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: change; Type: TABLE; Schema: public; Owner: gardener
--

CREATE TABLE public.change (
    dataset_id character varying(36) NOT NULL,
    datastore character varying(20) NOT NULL,
    path character varying NOT NULL,
    what character varying(20) NOT NULL,
    "when" timestamp without time zone NOT NULL,
    by character varying NOT NULL,
    "from" character varying,
    "to" character varying
);


ALTER TABLE public.change OWNER TO gardener;

--
-- Name: file; Type: TABLE; Schema: public; Owner: gardener
--

CREATE TABLE public.file (
    dataset_id character varying(36) NOT NULL,
    datastore character varying(20) NOT NULL,
    path character varying NOT NULL,
    status character varying(22),
    hash character varying(32),
    hashing_time double precision,
    sha256 character varying(64),
    last_modified_time timestamp without time zone,
    last_validated_time timestamp without time zone,
    last_verified_time timestamp without time zone,
    bytes bigint,
    format character varying
);


ALTER TABLE public.file OWNER TO gardener;

--
-- Name: status; Type: TABLE; Schema: public; Owner: gardener
--

CREATE TABLE public.status (
    name character varying(22) NOT NULL,
    description character varying,
    is_visible boolean,
    is_error boolean,
    is_checked boolean,
    is_validated boolean,
    priority integer
);


ALTER TABLE public.status OWNER TO gardener;

--
-- Data for Name: change; Type: TABLE DATA; Schema: public; Owner: gardener
--

COPY public.change (dataset_id, datastore, path, what, "when", by, "from", "to") FROM stdin;
\.


--
-- Data for Name: file; Type: TABLE DATA; Schema: public; Owner: gardener
--

COPY public.file (dataset_id, datastore, path, status, hash, hashing_time, sha256, last_modified_time, last_validated_time, last_verified_time, bytes, format) FROM stdin;
\.


--
-- Data for Name: status; Type: TABLE DATA; Schema: public; Owner: gardener
--

COPY public.status (name, description, is_visible, is_error, is_checked, is_validated, priority) FROM stdin;
CANCELLED	File was being moved when Cancel was called to stop move	f	f	t	f	1
ZIPPED_UNKNOWN_MISSING		t	t	f	f	0
UNKNOWN_MISSING	File exists but has since been removed	t	t	f	f	0
UNKNOWN	File exists but not known	t	t	t	f	0
ZIPPED		f	f	t	f	2
REMOVED_UNKNOWN	File was removed, but reappeared	t	t	t	f	0
ZIPPED_UNKNOWN		t	t	t	f	0
MOVED_UNKNOWN_MISSING	File had been moved, then reappeared, now it is missing	t	t	f	f	0
VALID	File is valid	t	f	t	t	2
MOVING_FROM_ERROR	Error occurred during move from this location	t	t	t	f	0
NO_HASH	File has been marked as VALID but there is no hash. Validation needs run to generate a hash	t	t	t	t	1
MOVING_TO_ERROR	Error occurred during move to this location	t	t	t	f	0
MOVED_UNKNOWN	File had been moved but now is present	t	t	t	f	0
CHANGED_HASH	MD5 checksum of file has changed	t	t	t	f	0
VALIDATING_HASH	File is currently being validated	t	f	t	f	1
WRITING	File is currently being written to disk	t	f	t	f	1
CHANGED_MTIME	Last modified time of file has changed	t	t	t	f	0
MISSING_UNKNOWN	File was missing but now exists	t	t	t	f	0
MISSING	File is missing from the datastore	t	t	f	f	0
REMOVED	File removed on purpose	f	f	f	f	2
MOVING_FROM	File is currently being moved from this location	t	f	t	f	1
MOVING_TO	File is currently being moved to this location	t	f	t	f	1
INVALID	Unknown issue with the file not covered by any other status	t	t	t	f	0
MOVED	File has moved to a new location	f	f	f	f	2
REMOVED_ERROR	Error attempting to remove file	t	t	t	f	0
\.


--
-- Name: change change_pkey; Type: CONSTRAINT; Schema: public; Owner: gardener
--

ALTER TABLE ONLY public.change
    ADD CONSTRAINT change_pkey PRIMARY KEY (dataset_id, datastore, path, what, "when", by);


--
-- Name: file file_pkey; Type: CONSTRAINT; Schema: public; Owner: gardener
--

ALTER TABLE ONLY public.file
    ADD CONSTRAINT file_pkey PRIMARY KEY (dataset_id, datastore, path);


--
-- Name: status status_pkey; Type: CONSTRAINT; Schema: public; Owner: gardener
--

ALTER TABLE ONLY public.status
    ADD CONSTRAINT status_pkey PRIMARY KEY (name);


--
-- Name: ix_status_is_checked; Type: INDEX; Schema: public; Owner: gardener
--

CREATE INDEX ix_status_is_checked ON public.status USING btree (is_checked);


--
-- Name: ix_status_is_validated; Type: INDEX; Schema: public; Owner: gardener
--

CREATE INDEX ix_status_is_validated ON public.status USING btree (is_validated);


--
-- Name: ix_status_is_visible; Type: INDEX; Schema: public; Owner: gardener
--

CREATE INDEX ix_status_is_visible ON public.status USING btree (is_visible);


--
-- Name: file file_status_fkey; Type: FK CONSTRAINT; Schema: public; Owner: gardener
--

ALTER TABLE ONLY public.file
    ADD CONSTRAINT file_status_fkey FOREIGN KEY (status) REFERENCES public.status(name);


--
-- PostgreSQL database dump complete
--

