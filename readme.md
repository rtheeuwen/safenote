#Dependencies

Desktop client:
- Linux, Gnome / KDE
- JRE 8

Server:
- JRE 8
- if Oracle JRE: JCE

#Description
Users have several alternatives when it comes to choosing a safeNote sharing app. Most available alternatives focus on usability over security.
Safenote is designed with security as its main point of focus. Users can be sure any safeNote they share between any of their devices cannot be read, or altered by anyone else.
- the server, which is used in the synchronization process, is assumed not to be trustworthy.
- to ensure secrecy, safeNotes are encrypted client side, with a secret key, before being sent to the server.
- to ensure integrity of any safeNotes received from the server, a locally created checksum, based on a local secret and safeNote content, is stored on the remote server together with the safeNote. Whenever a safeNote is served, the stored checksum is served with it. The client in turn recalculates a new checksum based on the received safeNote. If the new checksum does not match the received one, the safeNote is discarded.
- to ensure request authenticity, every message sent to the server is signed using a private key. The server validates every message it receives using the user's public key.
- the required cryptographic keys are stored on the user's filesystem and are never sent over a network. In order to share their key between devices, user's can transfer the key by means of scanning a qr code.

#Planned
- Android app
- SWT UI + Windows/OS X support

#Suggestions
roel.theeuwen@gmail.com
