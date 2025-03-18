# RoutePlanner
Bicycle routing app - Java / JavaFX / H2 Database

## Description

Route planner aplikacije je izrađena u Javi te je cilj bio napraviti aplikaciju koja bi omogućila jednostavan način za planiranje ruta vožnji biciklom i praćenje vožnji (ucrtavanje kretanja i praćenje bitnih metrika poput brzine, udaljenosti, vremena, itd.).

Aplikacija koristi SHA-256 algoritam sa promjenjivom soli i paprom za sigurnost pri prijavi, te su podatci o lokacijama spremljeni unutar enkriptiranih XML i JSON datoteka unutar baze podataka radi sigurnosti.

Za geolocation i routing se koristi GraphHopper API te se rute prikazuju na GluonHQ karti.

Aplikacija ima mogućnost kreiranja PDF izvještaja sa podatcima korisnika i svih spremljenih vožnji, te također ima mogućnost importa i exporta ruta u binarnom obliku kako bi se mogle dijeliti među korisnicima.

U izradi aplikacije na nekoliko mjesta su radi boljih performansi aplikacije korištene dretve i kritične sekcije

## Video

[![IMAGE ALT TEXT](http://img.youtube.com/vi/ms9pbfGjXt4/0.jpg)](http://www.youtube.com/watch?v=ms9pbfGjXt4 "Video Title")
