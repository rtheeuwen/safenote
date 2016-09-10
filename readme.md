#Dependencies
- Linux, Gnome / KDE
- JRE 8
- GTK webkit: https://pkgs.org/altlinux-sisyphus/classic-x86_64/libwebkitgtk2-2.4.11-alt1.x86_64.rpm.html
- Firefox
- If you are using the Oracle JRE you need the Java Cryptography extension: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html

#Description
Users have several alternatives when it comes to choosing a note sharing app. Most available alternatives focus on usability over security.
Safenote is designed with security as its main point of focus. Users can be sure any note they share between any of their devices cannot be read, or altered by anyone else:
- the server, which is used in the synchronization process, is assumed not to be trustworthy.
- to ensure secrecy, notes are encrypted with a secret key, client side, before being sent to the server.
- to ensure integrity of notes received from the server, a locally created checksum, based on a local secret and note content, is stored on the remote server. Whenever a note is served, the checksum is recalculated to insure the content of the note has not changed.
- to ensure request authenticity, every message sent to the server is signed using a private key. The server validates every message it receives using the user's public key.
- the required cryptographic keys are stored on the user's filesystem and are never sent over a network. In order to share their key between devices, user's can transfer the key by means of scanning a qr code.

#Planned
- Android app
- SWT UI + Windows/OS X support

#Contact
roel.theeuwen@gmail.com
https://github.com/rtheeuwen/safenote
Suggestions are more than welcome!