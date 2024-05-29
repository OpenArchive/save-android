# Open Archive "SAVE Space" Specification

Nathan Freitas aka @n8fr8
Guardian Project
Created: 21 November 2018
Updated: 9 July 2019

## Summary

This document is for outlining the folder and file structure for the publishing of an Open Archive
(OA) project, otherwise known as a "SAVE Space". This specifies how to serialize to a disk
(or a tar/zip archive file) the set of media files and metadata imported, organized and curated in
the Open Archive app.
It will also specify the format of the metadata included, and what extra data can also be included, 
such as OpenPGP signatures.

## Project

The current development work on the OA app adds a new concept of projects. A Project is a 
group of media files that have been imported, curated and annotated into a set within the OA app.

## Entries

An Entry is a unique media item within a Project and its associated metadata.

## Entry Metadata

Each Entry has an associated set of metadata which can be stored in memory, in a data store 
(relational, key-value, etc.) or serialized to a file. The default format for serialization is 
JavaScript Object Notation (JSON).

The default metadata fields are:

- author
- title (or subject)
- description
- dateCreated
- dateImported
- usage (license, terms of use)
- location
- tags
- contentType
- contentLength
- originalFileName
- hash (sha256 checksum)

Here is an example JSON format for this metadata:

```json
{
  "author": "Jay Jonah Jameson",
  "title": "The Amazing Spiderman",
  "dateCreated": "2012-04-23T18:25:43.511Z",
  "contentType": "image/jpeg",
  "contentLength": "987654321"
}
```

## Project Naming

In order to improve the ability for receivers of Collections to organize and parse what they are 
receiving, the OA app sets a name for the project. This name is used as a top level folder name for storing the uploaded itmes. 

Examples:

WebDavHome/User/Foo/`Family Photos`
WebDavHome/User/Foo/`War Crime Evidence`

## Project Submissions

Within each project folder, there will be one or more Project Submission folders. Each Submission folder will be named using the timestamp of when it was uploaded or added to the project.

The timestamp format is YYYY-MM-DD-THH-MM-SSZZZZ (or something like that!)

WebDavHome/User/Foo/`Family Photos`/2019-05-02T01:09:26GMT-04:00
WebDavHome/User/Foo/`Family Photos`/2019-04-29T12:47:43GMT-04:00
WebDavHome/User/Foo/`War Crime Evidence`/2018-11-02T01:09:26GMT-04:00

## Project Submissions File / Folder Structure

Within each submission folder, there will be one or more media files. For each media file, there will be a associated metadata file.

```
.
+-- `Family Photos`/
    +-- 2019-05-02T01:09:26GMT-04:00/
        +-- IMG_51238.jpg
        +-- IMG_51238.jpg.meta.json
        +-- VIDEO_51238.mp4
        +-- VIDEO_51238.mp4.meta.json
```

## Project FLAGGED File / Folder Structure

It is possible for the OA SAVE client to have media items that are flagged. The flag indicates some kind of special content state that only the user and their community can decide, but may be used for sensitive content that a user should be warned about before opening.

Within each submission folder, there may optionally be a folder named "FLAGGED". Any media items marked as flagged should be uploaded there, along with their metadata file.

```
.
+-- `Family Photos`/
    +-- 2019-05-02T01:09:26GMT-04:00/
        +-- IMG_51238.jpg
        +-- IMG_51238.jpg.meta.json
        +-- VIDEO_51238.mp4
        +-- VIDEO_51238.mp4.meta.json
        +-- FLAGGED/
            +-- VIDEO_911.mp4
            +-- VIDEO_911.mp4.meta.json
        
```

