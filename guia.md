# Catan · Hibernate — Guia de ruta

Feu els passos **en ordre** i no passeu al següent fins que l'anterior funcioni. Per saber si funciona, feu servir el **Guió de proves**.

## Els 7 passos

- [x] **1. Taules.** Les entitats i relacions de l'enunciat. Construccions amb herència JOINED. HexagonVertex és la N:M entre Hexagon i Vertex amb un atribut propi (posicio, de 0 a 5): igual que Llancament a teoria, és una entitat amb id propi, dos @ManyToOne i un @UniqueConstraint sobre la parella (hexagon, vertex). Des d'Hexagon, un @OneToMany(mappedBy). Ni @ManyToMany ni clau composta. Camps verificats: nullable, unique, length i mínims/màxims. Fet quan: el programa arrenca i crea les taules.
- [x] **2. IniciarPartida.** Entre 2 i 4 jugadors, ordre de torn aleatori, càrrega dels 4 CSV (vèrtexs, hexàgons, hexagon_vertex, arestes), terrenys i números sortejats, lladre al desert. Fet quan: cada partida té 19 hexàgons, 54 vèrtexs, 114 HexagonVertex i 72 arestes, i una 2a partida ho duplica.
- [x] **3. Mostrar i comptar punts.** MostrarTauler, MostrarRecursos, MostrarPartida i CalcularPunts, amb consultes a la BD (mínim 2 @NamedQuery). Fet quan: amb una partida nova es veu el tauler i tot és a zero.
- [x] **4. Preparació.** ConstruirPoblat, ConstruirCarretera i PassarTorn en fase PREPARACIO: ordre de serp, el segon poblat dóna recursos. Fet quan: 3 jugadors acaben la preparació i la partida passa a EN_CURS.
- [x] **5. Torn normal.** TirarDaus (amb producció), construir poblat i carretera, MillorarACiutat, ComerciarBanca, PassarTorn i ComprovarVictoria. Fet quan: podeu jugar una partida fins que algú guanya.
- [x] **6. (AVANÇAT)** Lladre (descart amb un 7 i MoureLladre), cartes (ComprarCarta, JugarCarta), ExercitMesGran i VertexsDisponibles en JPQL. Fet quan: un 7 bloqueja el torn fins que es mou el lladre.
- [x] **7. Lliurament.** readme.txt amb els noms de tots els membres, document de proves, codi net i comentat.

## Costos i límits

- Carretera: 1 FUSTA + 1 MAO (màx. 15)
- Poblat: 1 FUSTA + 1 MAO + 1 LLANA + 1 BLAT (màx. 5)
- Ciutat: 2 BLAT + 3 MINERAL (màx. 4)
- Carta: 1 BLAT + 1 LLANA + 1 MINERAL
- Banca: 4 recursos iguals per 1.

Les cartes de desenvolupament es creen en iniciar la partida (14 CAVALLER, 5 PUNT_VICTORIA i 2 MONOPOLI). Un 7 o un CAVALLER deixa el lladre pendent i impedeix continuar el torn fins a moure'l. El moviment roba un recurs aleatori d'una víctima adjacent amb recursos, o no roba si no n'hi ha. L'exèrcit més gran dona 2 punts a un únic líder amb almenys 3 cavallers.

En una tirada de 7, qui té més de 7 recursos descarta la meitat (arrodonint cap avall); amb 7 o menys no descarta. El lladre impedeix la producció a l'hexàgon que ocupa. Els casos avançats encara pendents d'execució consten com a planificats a `proves.md`.

## Quatre trucs que estalvien hores

- L'**id** és el de la BD; el **codi** és el del CSV i va al camp codi. No són el mateix.
- Feu **una sola comprovació comuna** (partida, torn, estat, daus tirats) i reutilitzeu-la a tots els mètodes. Cada error, amb un missatge diferent.
- Comproveu-ho tot **abans** de descomptar recursos o crear res.
- L'aresta no té sentit: per trobar els veïns d'un vèrtex, mireu-lo com a vertexA i com a vertexB.

## Què NO cal fer

Dibuixar el tauler, guardar x i y, carretera més llarga, ports, comerç entre jugadors, mètodes extra que l'enunciat no demana, ni la part Spring (és la segona entrega).

# Catan · Hibernate — Guió de proves i autoavaluació

## Abans de començar

- El tauler, els ids i l'ordre de torn són **aleatoris**: comproveu missatges i comptes, no valors concrets.
- Per donar recursos o abaixar els punts per guanyar feu servir SQL directe, per exemple UPDATE RecursJugador SET quantitat = 5 WHERE jugador_id = 1; i UPDATE Partida SET puntsPerGuanyar = 4 WHERE id = 1; (adapteu els noms de columna als vostres).
- Per provar la producció cal una tirada coneguda: afegiu un mètode de proves que la fixi i esborreu-lo abans de lliurar.
- Quan una acció falla, **no ha de canviar res** a la BD.

## Les proves

| Pas | Prova | Ha de passar | ✔/✘ |
|---|---|---|---|
| 1 | Executar amb la BD buida | Es creen les taules; totes tenen id autogenerat; Poblat i Ciutat pengen d'Assentament, i aquest i Carretera, de Construccio | [x] |
| 2 | IniciarPartida amb 1 jugador, amb 5 i amb 3 | Els dos primers fallen i no creen res; el tercer torna l'id de la partida | [x] |
| 2 | Comptar les files del tauler d'una partida | 19 hexàgons, 54 vèrtexs, 72 arestes i 114 HexagonVertex | [x] |
| 2 | Terrenys i números | 4 BOSC, 4 PASTURES, 4 CAMPS, 3 TURONS, 3 MUNTANYES i 1 DESERT; el desert sense número i amb el lladre; cap 7 | [x] |
| 2 | Un cas concret a la 1a partida | Hexàgon 10: vèrtexs 32, 33, 21, 20, 19 i 31 (posicions 0 a 5). Vèrtex 32: hexàgons 10, 14 i 15, i veïns 33, 31 i 43 | [x] |
| 2 | Crear una 2a partida | Tot es duplica i els ids dels hexàgons ja no coincideixen amb el codi | [x] |
| 3 | MostrarTauler, MostrarRecursos i MostrarPartida d'una partida nova | 19 hexàgons; recursos a 0; PREPARACIO, ronda 1, actiu = ordre 1. Amb un id inexistent, un missatge i cap excepció | [x] |
| 4 | Preparació: construir fora de torn; poblat massa a prop d'un altre; carretera que no toca el poblat nou; segon poblat en el mateix torn | Cada cas falla amb el seu missatge | [x] |
| 4 | Preparació completa amb 3 jugadors | Ordre de serp (1, 2, 3, 3, 2, 1); el 2n poblat dóna 1 recurs per hexàgon (no desert); al final, EN_CURS i ronda 1 | [x] |
| 5 | Construir o passar torn abans de tirar els daus; tirar-los dos cops | Error en els dos casos | [x] |
| 5 | Tirada igual al número d'un hexàgon amb poblat (o ciutat) | El propietari rep 1 recurs (2 si és ciutat); els altres, res; amb un 7, ningú | [x] |
| 5 | Construir sense recursos, en lloc ocupat, massa a prop, sense connexió i amb el límit assolit | Cada cas falla amb un missatge diferent i **no es descompta cap recurs** | [x] |
| 5 | MillorarACiutat i ComerciarBanca | El poblat passa a ciutat al mateix vèrtex; 4 recursos iguals donen 1 (amb 3, o amb dona igual a rep, error) | [x] |
| 5 | PassarTorn i CalcularPunts | La ronda puja quan el torn torna a l'ordre 1; poblat = 1 punt, ciutat = 2 | [x] |
| 5 | Abaixar els punts per guanyar i construir fins a arribar-hi | La partida passa a FINALITZADA amb guanyador, i després cap acció funciona | [x] |
| 6 | **(AVANÇAT)** Tirada de 7 | Qui té més de 7 recursos en descarta la meitat; el torn queda bloquejat fins a MoureLladre; l'hexàgon amb el lladre no produeix | [ ] |
| 6 | **(AVANÇAT)** Cartes | Pila de 21 (14 CAVALLER, 5 PUNT_VICTORIA, 2 MONOPOLI); no es juga una carta comprada aquesta ronda; CAVALLER deixa el lladre pendent; MONOPOLI recull el recurs; PUNT_VICTORIA suma 1 | [ ] |
| 6 | **(AVANÇAT)** ExercitMesGran i VertexsDisponibles | Exèrcit: mínim 3 cavallers i sense empat (+2 punts). VertexsDisponibles: només els vèrtexs on ConstruirPoblat no falla per lloc | [ ] |
| 7 | Lliurament | readme.txt, 2 @NamedQuery, document de proves amb captures i noms de tots; prova en fred amb la BD buida | [x] |

# Catan · Hibernate — Rúbrica d'avaluació

**Total 10 punts.** Part base 6,5 (5,5 de funcionalitat + 0,5 document de proves + 0,5 codi net i JPA) i part avançada 3,5.

## Com es qualifica

- Cada criteri té una llista de **comprovacions** (més avall). Una comprovació és sí o no, sense punts intermedis.
- Una comprovació és sí només si es reprodueix executant el projecte sobre una BD buida amb la configuració del readme.txt. Les marcades **(codi)** es comproven llegint el codi.
- El nivell surt del nombre de sí, aplicant aquest ordre i parant al primer que es compleix:
- totes → Excel·lent
- totes menys una → Notable
- la meitat o més (arrodonint cap amunt) → Suficient
- almenys una → Insuficient
- cap → No assolit.
- Punts del criteri = pes × percentatge del nivell (taula de sota). La nota és la suma, arrodonida a un decimal al final.
| Pes del criteri | Excel·lent 100% | Notable 75% | Suficient 50% | Insuficient 25% | No assolit |
|---|---|---|---|---|---|
| 1,00 | 1,00 | 0,75 | 0,50 | 0,25 | 0 |
| 0,75 | 0,75 | 0,56 | 0,38 | 0,19 | 0 |
| 0,50 | 0,50 | 0,38 | 0,25 | 0,13 | 0 |

## Graella de la rúbrica

Cada cel·la diu quantes comprovacions sí calen per arribar al nivell.

| Criteri | Pes | Excel·lent | Notable | Suficient | Insuficient | No assolit |
|---|---|---|---|---|---|---|
| **PART BASE** |  |  |  |  |  |  |
| 1 · Model i BD | 1,00 | 6 de 6 | 5 de 6 | 3 o 4 de 6 | 1 o 2 de 6 | 0 |
| 2 · IniciarPartida i CSV | 1,00 | 6 de 6 | 5 de 6 | 3 o 4 de 6 | 1 o 2 de 6 | 0 |
| 3 · Consultes de lectura i punts | 0,75 | 6 de 6 | 5 de 6 | 3 o 4 de 6 | 1 o 2 de 6 | 0 |
| 4 · Fase de preparació | 1,00 | 6 de 6 | 5 de 6 | 3 o 4 de 6 | 1 o 2 de 6 | 0 |
| 5 · TirarDaus i producció | 0,50 | 5 de 5 | 4 de 5 | 3 de 5 | 1 o 2 de 5 | 0 |
| 6 · Construir i millorar | 0,75 | 6 de 6 | 5 de 6 | 3 o 4 de 6 | 1 o 2 de 6 | 0 |
| 7 · Banca, torn i victòria | 0,50 | 5 de 5 | 4 de 5 | 3 de 5 | 1 o 2 de 5 | 0 |
| 8 · Document de proves | 0,50 | 5 de 5 | 4 de 5 | 3 de 5 | 1 o 2 de 5 | 0 |
| 9 · Codi net i JPA | 0,50 | 6 de 6 | 5 de 6 | 3 o 4 de 6 | 1 o 2 de 6 | 0 |
| **Subtotal base** | **6,50** |  |  |  |  |  |
| **PART AVANÇADA** |  |  |  |  |  |  |
| 10 · Lladre | 1,00 | 5 de 5 | 4 de 5 | 3 de 5 | 1 o 2 de 5 | 0 |
| 11 · Cartes de desenvolupament | 1,00 | 6 de 6 | 5 de 6 | 3 o 4 de 6 | 1 o 2 de 6 | 0 |
| 12 · Exèrcit més gran | 0,50 | 4 de 4 | 3 de 4 | 2 de 4 | 1 de 4 | 0 |
| 13 · VertexsDisponibles | 1,00 | 5 de 5 | 4 de 5 | 3 de 5 | 1 o 2 de 5 | 0 |
| **Subtotal avançada** | **3,50** |  |  |  |  |  |

## Comprovacions de la part base

### 1 · Model i BD (1,00)

- Herència JOINED: Poblat i Ciutat penjen d'Assentament, i Assentament i Carretera de Construccio, amb la mateixa PK.
- HexagonVertex és una entitat amb id propi i dues FK, i no hi ha cap @ManyToMany ni clau composta a tot el model.
- La BD garanteix un sol assentament per vèrtex, una sola carretera per aresta i un sol RecursJugador per (jugador, tipus).
- lladre, jugadorActiu i guanyador de Partida permeten NULL.
- La BD rebutja un UPDATE amb un valor fora de rang en almenys 4 camps numèrics diferents (per exemple, número d'hexàgon 7, quantitat -1, posició 6).
- (codi) Cap camp de text sense length i cap camp obligatori declarat nullable.

### 2 · IniciarPartida i CSV (1,00)

- Amb 1 i amb 5 jugadors no es crea cap fila i es respon amb un error.
- Una partida crea 19 hexàgons, 54 vèrtexs, 72 arestes i 114 HexagonVertex; una 2a partida n'afegeix el mateix nombre i els ids dels hexàgons ja no coincideixen amb el codi.
- Terrenys: 4 BOSC, 4 PASTURES, 4 CAMPS, 3 TURONS, 3 MUNTANYES i 1 DESERT. El desert sense número; els altres 18 tenen 2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12.
- Estat PREPARACIO, ronda 1, jugador actiu = ordre 1, guanyador NULL, lladre al desert i 5 RecursJugador a 0 per jugador.
- En 5 execucions amb la mateixa llista de noms l'ordre de torn no surt sempre igual; els ordres són 1..n sense repetir i els colors segueixen l'ordre.
- A la 1a partida, l'hexàgon 10 té els vèrtexs 32, 33, 21, 20, 19 i 31 a les posicions 0 a 5, i el vèrtex 32 toca els hexàgons 10, 14 i 15 amb veïns 33, 31 i 43.

### 3 · Consultes de lectura i punts (0,75)

- MostrarTauler mostra els 19 hexàgons amb terreny i número, i el desert amb el lladre.
- MostrarTauler mostra poblats i ciutats amb el propietari un cop construïts, i no mostra carreteres.
- MostrarRecursos mostra els 5 tipus amb la quantitat.
- MostrarPartida mostra estat, ronda, jugador actiu i última tirada, i per jugador punts, poblats, ciutats, carreteres i només el total de recursos.
- CalcularPunts dona 2 amb 2 poblats, 3 amb un poblat més i 4 després de millorar-ne un a ciutat.
- Amb un id inexistent, les quatre funcions responen amb un missatge i sense excepció.

### 4 · Fase de preparació (1,00)

- Es rebutja construir fora de torn, una carretera abans del poblat, un 2n poblat en el mateix torn i una 2a carretera en el mateix torn.
- Es rebutja un poblat en un vèrtex ocupat, massa a prop d'un altre (regla de la distància), d'una altra partida o inexistent.
- Només s'accepta una carretera que toca el poblat acabat de col·locar, també a la ronda 2.
- PassarTorn es rebutja si falta el poblat o la carretera, i l'ordre és de serp (J1, J2, J3, J3, J2, J1).
- El 1r poblat no dona recursos i el 2n en dona 1 per cada hexàgon no desert que toca.
- En acabar, la partida és EN_CURS, ronda 1, actiu = ordre 1, i cada jugador té 2 poblats, 2 carreteres i 2 punts. Abans, TirarDaus i les accions d'EN_CURS es rebutgen.

### 5 · TirarDaus i producció (0,50)

- Es rebutja un 2n TirarDaus en el mateix torn, una partida inexistent i una partida que no és EN_CURS.
- Es guarda la tirada (sempre entre 2 i 12 en 30 tirades), dausTirats passa a cert i es torna un resum.
- Un poblat adjacent a l'hexàgon amb el número tirat rep 1 recurs del terreny; qui no té cap assentament adjacent no rep res.
- Una ciutat rep 2 recursos, i un poblat que toca dos hexàgons amb el mateix número en rep 1 per cada hexàgon.
- Amb un 7 cap recurs canvia (a la part base).

### 6 · Construir i millorar (0,75)

- ConstruirCarretera, ConstruirPoblat i MillorarACiutat es rebutgen fora de torn, abans de tirar els daus i amb la partida FINALITZADA.
- Carretera: es rebutja ocupada, no connectada, sense recursos i la núm. 16; la correcta descompta 1 FUSTA i 1 MAO.
- Poblat: es rebutja ocupat, massa a prop, sense carretera pròpia, sense recursos i el núm. 6; el correcte descompta 1 FUSTA, 1 MAO, 1 LLANA i 1 BLAT.
- Ciutat: es rebutja sense poblat propi al vèrtex, sense recursos i la núm. 5; la correcta substitueix el poblat al mateix vèrtex i descompta 2 BLAT i 3 MINERAL.
- Una acció rebutjada per falta de recursos no modifica cap recurs ni crea cap fila (provat amb una acció de cada tipus).
- Les situacions d'error donen com a mínim 8 missatges diferents.

### 7 · Banca, torn i victòria (0,50)

- ComerciarBanca: 4 recursos iguals donen 1; es rebutja amb 3, amb dona igual a rep i fora de torn.
- PassarTorn es rebutja sense haver tirat; si no, l'actiu és el següent i dausTirats torna a fals.
- La ronda puja només quan el torn torna a l'ordre 1.
- En arribar als punts per guanyar (baixats per SQL), la partida és FINALITZADA i es guarda el guanyador.
- Una partida FINALITZADA rebutja TirarDaus, construir, comerciar i passar torn.

### 8 · Document de proves (0,50)

- Hi ha com a mínim una prova per cada mètode públic implementat.
- Cada prova indica les dades d'entrada, el resultat esperat i el resultat obtingut.
- Cada mètode que pot rebutjar una acció té com a mínim una prova de cas rebutjat.
- Almenys el 80% de les proves porten evidència (captura de consola o de la BD).
- De 5 proves marcades com a correctes i triades pel professorat, les 5 es reprodueixen amb el mateix resultat.

### 9 · Codi net i JPA (0,50)

- (codi) Tots els mètodes de les interfícies DAO tenen un comentari que n'explica el propòsit.
- (codi) Cap bloc de codi comentat ni System.out de depuració fora dels mètodes Mostrar* i del Main.
- (codi) Mínim 2 @NamedQuery, amb paràmetres amb nom (cap consulta feta concatenant text).
- (codi) Cada operació principal té interfície i implementació DAO, i el Main no conté lògica del joc.
- (codi) Cap missatge d'error escrit com a literal en dos mètodes diferents (estan en constants).
- (codi) Cap dada del tauler escrita al codi: vèrtexs, arestes i posicions surten dels CSV.

## Comprovacions de la part avançada

### 10 · Lladre (1,00)

- Amb un 7, qui té més de 7 recursos en descarta la meitat (arrodonint cap avall), qui en té 7 o menys no descarta, i no hi ha producció.
- Amb el lladre pendent es rebutgen construir, comerciar, comprar carta i passar torn.
- MoureLladre es rebutja per a un jugador que no és l'actiu, sense lladre pendent, al mateix hexàgon i amb una víctima no vàlida (sense assentament a l'hexàgon o el mateix jugador).
- Amb una víctima vàlida, es roba 1 recurs (el total entre tots no canvia) i lladrePendent passa a fals; si no hi ha cap víctima possible es mou sense robar.
- L'hexàgon amb el lladre no produeix, i quan el lladre se'n va, torna a produir.

### 11 · Cartes de desenvolupament (1,00)

- Cada partida té una pila de 21 cartes: 14 CAVALLER, 5 PUNT_VICTORIA i 2 MONOPOLI.
- ComprarCarta es rebutja sense recursos, fora de torn i amb la pila buida; la correcta descompta 1 BLAT, 1 LLANA i 1 MINERAL i guarda la ronda de compra.
- JugarCarta es rebutja si la carta s'ha comprat aquesta ronda, és d'un altre jugador, ja està usada o és PUNT_VICTORIA.
- Jugar un CAVALLER suma 1 als cavallers jugats, marca la carta com a usada i deixa el lladre pendent.
- Jugar un MONOPOLI recull tot el recurs indicat dels altres jugadors, i la suma total no canvia.
- Cada carta PUNT_VICTORIA suma 1 punt a CalcularPunts i a MostrarPartida.

### 12 · Exèrcit més gran (0,50)

- Amb cap jugador amb 3 cavallers o més, no retorna cap jugador.
- Retorna el jugador amb 3 cavallers o més que té el màxim sense empat.
- Amb un empat al màxim no retorna cap jugador.
- CalcularPunts suma 2 punts a aquest jugador, i si això li fa arribar als punts per guanyar, la partida passa a FINALITZADA.

### 13 · VertexsDisponibles (1,00)

- (codi) Està resolt amb una consulta JPQL o HQL, sense un bucle sobre tots els vèrtexs.
- A PREPARACIO retorna els vèrtexs lliures sense veïns ocupats, sense exigir carretera.
- A EN_CURS retorna només vèrtexs lliures, sense veïns ocupats i amb carretera pròpia adjacent.
- En 5 vèrtexs de la llista triats pel professorat, ConstruirPoblat (amb recursos) no falla per lloc.
- En 5 vèrtexs fora de la llista triats pel professorat, ConstruirPoblat falla per lloc (ocupat, distància o connexió).
