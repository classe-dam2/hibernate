# Catan · Hibernate — Document de proves

Totes les proves s'executen amb `Main` sobre una BD buida. El tauler, els ids i l'ordre de torn són aleatoris: es comproven missatges i comptes, no valors concrets.

Columna **Evidència**: número de la captura de consola (a fer per l'equip). Columna **Estat**: ✔ executada i correcta.

## iniciarPartida

| # | Entrada | Esperat | Obtingut | Evidència | Estat |
|---|---|---|---|---|---|
| 1 | 1 jugador | Error, no es crea res | "Calen entre 2 i 4 jugadors" | C1 | ✔ |
| 2 | 5 jugadors | Error, no es crea res | "Calen entre 2 i 4 jugadors" | C1 | ✔ |
| 3 | 3 jugadors (Anna, Pau, Marta) | Torna l'id de la partida | "Partida creada amb id 1" | C1 | ✔ |
| 4 | Tauler de la partida nova | 19 hexàgons, desert amb lladre i sense número | 19 hexàgons, desert `[LLADRE]` sense número | C2 | ✔ |
| 5 | Estat inicial | PREPARACIO, ronda 1, actiu = ordre 1, 5 recursos a 0 per jugador | Ho mostra `MostrarPartida` i `MostrarRecursos` | C2 | ✔ |

## MostrarTauler, MostrarPartida, MostrarRecursos, CalcularPunts

| # | Entrada | Esperat | Obtingut | Evidència | Estat |
|---|---|---|---|---|---|
| 6 | Partida nova | Hexàgons i recursos a 0 | Correcte | C2 | ✔ |
| 7 | Partida 9999 (tauler i partida) | Missatge, sense excepció | "La partida 9999 no existeix." | C2 | ✔ |
| 8 | Jugador 9999 (recursos i punts) | Missatge, sense excepció; punts 0 | "El jugador 9999 no existeix." / 0 | C2 | ✔ |
| 9 | Punts amb 2 poblats | 2 | 2 | C9 | ✔ |
| 10 | Punts després de millorar un poblat a ciutat | +1 (poblat 1, ciutat 2) | Passa de 3 a 4 | C9 | ✔ |

## ConstruirPoblat / ConstruirCarretera (PREPARACIO)

| # | Entrada | Esperat | Obtingut | Evidència | Estat |
|---|---|---|---|---|---|
| 11 | Poblat d'un jugador que no és l'actiu | Rebutjat | "No és el teu torn." | C3 | ✔ |
| 12 | Carretera abans del poblat | Rebutjat | "Primer has de col·locar un poblat." | C3 | ✔ |
| 13 | Poblat en un vèrtex inexistent | Rebutjat | "El vèrtex no existeix en aquesta partida." | C3 | ✔ |
| 14 | 2n poblat en el mateix torn | Rebutjat | "Ja has col·locat el poblat d'aquest torn." | C4 | ✔ |
| 15 | 2a carretera en el mateix torn | Rebutjat | "Ja has col·locat la carretera d'aquest torn." | C4 | ✔ |
| 16 | Poblat veí d'un altre (vèrtex 46, veí del 54) | Rebutjat | "Massa a prop d'una altra construcció." | C4 | ✔ |
| 17 | Carretera que no toca el poblat nou (aresta 1) | Rebutjada | "La carretera ha de tocar el poblat que acabes de col·locar." | C4 | ✔ |

## PassarTorn

| # | Entrada | Esperat | Obtingut | Evidència | Estat |
|---|---|---|---|---|---|
| 18 | A PREPARACIO sense poblat | Rebutjat | "No pots passar el torn: falta col·locar el poblat." | C3 | ✔ |
| 19 | Preparació de 3 jugadors | Ordre 1, 2, 3, 3, 2, 1; al final EN_CURS, ronda 1, actiu = ordre 1 | Ordre correcte; "Preparació acabada" | C4 | ✔ |
| 20 | 2n poblat de cada jugador | 1 recurs per hexàgon no desert | Anna 3, Marta 2, Pau 1 (segons terrens) | C4 | ✔ |
| 21 | A EN_CURS sense tirar els daus | Rebutjat | "Primer has de tirar els daus." | C5 | ✔ |
| 22 | Una ronda completa | La ronda puja a 2 en tornar a l'ordre 1 | Ronda 2 | C6 | ✔ |

## TirarDaus

| # | Entrada | Esperat | Obtingut | Evidència | Estat |
|---|---|---|---|---|---|
| 23 | A PREPARACIO | Rebutjat | "La partida no està en curs." | C3 | ✔ |
| 24 | Dos cops en el mateix torn | Rebutjat el 2n | "Ja has tirat els daus aquest torn." | C6 | ✔ |
| 25 | Tirada igual al número d'hexàgons amb poblat | 1 recurs per poblat (1 per hexàgon) | Ex.: "Anna rep 1 de LLANA" | C6 | ✔ |
| 26 | Un poblat que toca dos hexàgons amb el mateix número | 1 per hexàgon | "Marta rep 1 de FUSTA" ×2 amb un 12 | C6 | ✔ |
| 27 | Tirada d'un 7 | Cap recurs | "Ha sortit un 7: no hi ha producció." | C9 | ✔ |
| 28 | Una ciutat adjacent a l'hexàgon | 2 recursos | "Anna rep 2 de LLANA" | C8 | ✔ |

## ConstruirCarretera / ConstruirPoblat / MillorarACiutat (EN_CURS)

| # | Entrada | Esperat | Obtingut | Evidència | Estat |
|---|---|---|---|---|---|
| 29 | Construir o millorar abans de tirar | Rebutjat | "Primer has de tirar els daus." | C5 | ✔ |
| 30 | Carretera sense recursos | Rebutjada, no canvia res | "Recursos insuficients (1 FUSTA i 1 MAO)." | C7 | ✔ |
| 31 | Carretera no connectada (aresta 1) | Rebutjada | "La carretera ha d'estar connectada a una construcció o carretera teva." | C7 | ✔ |
| 32 | Carretera en aresta ocupada | Rebutjada | "L'aresta ja té una carretera." | C9 | ✔ |
| 33 | Poblat sense carretera connectada | Rebutjat | "El poblat ha d'estar connectat a una carretera teva." | C9 | ✔ |
| 34 | Poblat connectat però sense recursos | Rebutjat, no canvia res | "Recursos insuficients (1 FUSTA, 1 MAO, 1 LLANA i 1 BLAT)." | C7 | ✔ |
| 35 | Poblat en vèrtex ocupat | Rebutjat | "El vèrtex ja està ocupat." | C9 | ✔ |
| 36 | Millorar un vèrtex sense poblat propi | Rebutjat | "Al vèrtex no hi ha cap poblat teu." | C9 | ✔ |
| 37 | Millorar sense recursos | Rebutjat, no canvia res | "Recursos insuficients (2 BLAT i 3 MINERAL)." | C7 | ✔ |
| 38 | Millorar un poblat propi | El poblat passa a ciutat al mateix vèrtex | "Poblat millorat a ciutat al vèrtex 54." | C9 | ✔ |
| 39 | Carretera núm. 16 | Rebutjada | "Has arribat al límit de 15 carreteres." | C10 | ✔ |
| 40 | Poblat núm. 6 | Rebutjat | "Has arribat al límit de 5 poblats." | C10 | ✔ |
| 41 | Ciutat núm. 5 | Rebutjada | "Has arribat al límit de 4 ciutats." | C10 | ✔ |

## ComerciarBanca

| # | Entrada | Esperat | Obtingut | Evidència | Estat |
|---|---|---|---|---|---|
| 42 | Abans de tirar els daus | Rebutjat | "Primer has de tirar els daus." | C5 | ✔ |
| 43 | Mateix recurs a donar i rebre | Rebutjat | "El recurs que dónes i el que reps no poden ser iguals." | C9 | ✔ |
| 44 | Amb 3 recursos (3 LLANA) | Rebutjat, no canvia res | "Cal tenir 4 recursos iguals per comerciar amb la banca." | C7 | ✔ |
| 45 | 4 BLAT per 1 MAO | BLAT −4, MAO +1 | BLAT 10→6, MAO 10→11 | C9 | ✔ |

## ComprovarVictoria

| # | Entrada | Esperat | Obtingut | Evidència | Estat |
|---|---|---|---|---|---|
| 46 | Punts per guanyar baixats a 5 per SQL i construir fins a arribar-hi | FINALITZADA amb guanyador | "Anna ha guanyat la partida amb 5 punts!" | C9 | ✔ |
| 47 | Tirar daus, passar torn, construir i comerciar amb la partida finalitzada | Tot rebutjat | "La partida ja ha finalitzat." ×4 | C9 | ✔ |
| 48 | Consulta amb nom Jugador.perPartida (dins MostrarPartida) | Jugadors ordenats per ordre de torn | "Jugador 1: Marta ... Jugador 3: Pau" | C2 | ✔ |
| 49 | Consulta amb nom Partida.perEstat (FINALITZADA i EN_CURS) | Recompte i ronda de cada partida | "Partides FINALITZADA: 1" / "Partides EN_CURS: 1" | C11 | ✔ |

## Proves avançades planificades (no executades)

Els casos d'aquesta secció descriuen els resultats esperats segons la funcionalitat implementada. No s'han executat ni tenen captures; els registres ✔ anteriors són històrics i no s'han alterat.

| # | Entrada | Esperat | Obtingut | Evidència | Estat |
|---|---|---|---|---|---|
| P1 | Tirada de 7: jugador amb 8 recursos totals, altre amb 7 o menys | El primer descarta 4 recursos; el segon no descarta; cap hexàgon produeix; lladre pendent | No executat | — | Planificat |
| P2 | Lladre pendent; provar construir, comerciar, comprar carta i passar torn | Cada acció es rebutja fins a MoureLladre | No executat | — | Planificat |
| P3 | MoureLladre com a jugador no actiu, sense pendent, al mateix hexàgon, o indicant víctima invàlida | Cada moviment es rebutja; cap canvi parcial | No executat | — | Planificat |
| P4 | Moure el lladre a hexàgon vàlid amb víctima adjacent amb recursos; repetir sense víctima possible | En primer cas es transfereix un recurs aleatori; altrament no es roba; pendent queda fals | No executat | — | Planificat |
| P5 | Comprar carta amb recursos suficients, sense recursos, fora de torn i amb pila buida | Compra vàlida gasta 1 BLAT, 1 LLANA i 1 MINERAL; invàlides es rebutgen | No executat | — | Planificat |
| P6 | Jugar CAVALLER comprat abans d'aquesta ronda; intentar jugar carta de ronda actual, aliena, usada i PUNT_VICTORIA | Cavaller vàlid queda usat, suma un cavaller i deixa lladre pendent; casos invàlids es rebutgen | No executat | — | Planificat |
| P7 | Jugar MONOPOLI sobre un recurs | El jugador rep tot aquell recurs dels altres; suma total es conserva | No executat | — | Planificat |
| P8 | Calcular punts amb PUNT_VICTORIA i llideratge d'exèrcit: cap amb 3, líder únic, empat al màxim, victòria pels 2 punts | Sense líder per sota de 3 o empat; líder únic rep +2; puntuació guanyadora finalitza partida | No executat | — | Planificat |
| P9 | VertexsDisponibles en PREPARACIO i EN_CURS | Retorna vèrtexs lliures sense veí ocupat; exigeix carretera pròpia només en EN_CURS | No executat | — | Planificat |
| P10 | Comparar vèrtexs retornats i no retornats amb ConstruirPoblat i recursos suficients | Els retornats passen validació de lloc; els exclosos fallen per ocupació, distància o manca de connexió | No executat | — | Planificat |
