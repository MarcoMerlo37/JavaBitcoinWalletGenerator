# Generatore di Wallet Bitcoin in Java
Questo è un generatore di Wallet Bitcoin implementato utilizzando tecnologie Java.
## Riferimento
Il prototipo è stato creato prendendo come spunto il sito [bitaddress.org](https://www.bitaddress.org/bitaddress.org-v3.3.0-SHA256-dec17c07685e1870960903d8f58090475b25af946fe95a734f88408cef4aa194.html), attraverso cui è possibile generare un Wallet Bitcoin (Chiave privata + indirizzo Bitcoin) a partire dai movimenti del cursore sullo schermo o dall'inserimento di caratteri in un campo di testo, che generano l'entropia tramite la quale vengono generate le chiavi. 
## Tecnologie utilizzate
Il sito sopra citato utilizza tecnologie Javascript, mentre questo prototipo utilizza API Java come MessageDigest o SecureRandom, con algoritmi di generazione di numeri random come "SHA1PRNG" e algoritmi di hash come "SHA-256".
### Librerie
Sono state utilizzate due librerie esterne:
* **Bouncycastle** - usato per replicare l'algoritmo di curvatura ellittica utilizzato in ambiente Bitcoin
* **Zxing** - utilizzato per generare codici QR a partire dalle chiavi generate

Questo progetto è stato sviluppato come prova pratica per l'esame di Sicurezza dell'Informazione M presso l'Alma Mater Studiorum UniBO.
