
#### jos&eacute; is a graphical chess tool.

You can store chess games in a database (backed by
MySQL).

You can view games and edit variations and comments.
You can play against a "plugged-in" chess engine and use it for
analysis.

<a href="https://peteschaefer.github.io/jose/images/shots/shot01.png"><img src="https://peteschaefer.github.io/jose/images/shots/shot01t.jpg"></a>
<a href="https://peteschaefer.github.io/jose/images/shots/shot04.png"><img src="https://peteschaefer.github.io/jose/images/shots/shot04t.jpg"></a>
<a href="https://peteschaefer.github.io/jose/images/shots/shot05.png"><img src="https://peteschaefer.github.io/jose/images/shots/shot05t.jpg"></a>

<a href="https://peteschaefer.github.io/jose/images/shots/index.html">more screen shots...</a>

---

Features:

- Graphical frontend to game database
- Read and write PGN (Portable Game Notations) files
- 2D and 3D view
- Edit games, insert comments, variations
- Bundled with [Leela Chess Zero](https://lczero.org/) and [Stockfish](https://stockfishchess.org/) for play and analysis; plug in any [UCI](https://peteschaefer.github.io/jose/links.html#engines) engine.
- Play Fischer Random Chess / Chess 960, or Shuffle Chess
- Opening Books and access to Lichess Opening Explorer
- ECO opening classification
- Use Chessnut Air / Air+ / Pro electronic boards
- Position Search
- Create HTML and PDF files.
- a (small) Web App

---

#### **<a href="https://github.com/peteschaefer/jose/releases">Download Current Version 1.5.2 and Patch 1.5.15.</a>**

<br>
<a href="https://github.com/peteschaefer/jose/wiki/Installation-Notes">Installation Notes</a><br>
<a href="https://github.com/peteschaefer/jose/wiki/What's-New-in-version-1.5">What's new ?</a><br>
<br>
<a href="https://peteschaefer.github.io/jose/links.html">Related Links</a>

---

## Windows Hotfix

jose would not launch at all and present an error dialog instead. 
This bug has been around for a while.

Please apply the latest [patch 1.5.15](https://github.com/peteschaefer/jose/releases/download/1.5.15rc1/jose-1515-patch.zip)

---

## macOS Hotfix

There was a bug that caused jose to stop working after some time on macOS. 

Quite annoying.

Here's how you can fix it:

* open a Terminal window
* navigate to the jose application folder
* then type
```
cd jose.app/Contents/MacOS
./shc -e 01/01/2199 -o jose -f jose.sh
```
and everything should be fine again.

<sub>(background info: Contents/MacOS/jose is nothing but a tiny compiled version of the shell script jose.sh. Strangely enough, the executable had an *expiry date* that your unmindful maintainer was not aware of.)</sub>

---

-- Peter Schäfer
