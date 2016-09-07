#Dependencies
Currently this app works correctly only on Linux (tested on Fedora)
- JRE 8
- GTK webkit: https://pkgs.org/altlinux-sisyphus/classic-x86_64/libwebkitgtk2-2.4.11-alt1.x86_64.rpm.html
- Firefox
- If you are using the Oracle JRE you need the Java Cryptography extension: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html

#Description
Several applications note taking applications exist which enable their users to take notes on a device and instantly synchronize these notes with other devices. Most of these apps do not apply application level and/or transport level encryption, resulting in several security vulnerabilities. Most importantly:
- Man-in-the-middle attack (for example, synchronizing notes using a public wifi-network);
- The party hosting the server has access to all notes.
SafeNote takes these, and several other security risks into account, and mitigates them by offering full client-side encryption. Additionally SafeNote assumes neither the synchronization server, or the integrity of the data stored on it, can be trusted. As a result, users can be sure any notes they write will be seen by their eyes only.

#Status
Currently an initial version is in development which, as-is, enables users to create notes and store remote back-ups, using a Linux desktop client. No note sharing functionality between devices is offered yet. I am planning to finish the desktop client in a couple of months and then proceed to develop an Android app.

#Technology used
The client application is written mostly in Java SE8 and relies on Spring, Hibernate, Jetty and SWT. Notes are stored locally in an embedded H2 database. Note editing services are exposed via a restful API, which runs on an embedded Jetty Server. An SWT shell is used to display Angular JS pages that consume the restful services. The service layer of the application offers Crypto services, as well as full text search and synchronization to the remote SafeNote server.

I am a relatively inexperienced Java developer and I’m developing this application mainly to broaden my knowledge and experience . Your advise and criticism is much appreciated and more than welcome.