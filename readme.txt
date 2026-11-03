CATAN - HIBERNATE
=================

Membres de l'equip
------------------
- Adria Cabrera
- Victoria Alcazar

Requisits
---------
- Java 21 o superior
- Maven
- MySQL en marxa a localhost:3306

Configuració de la base de dades
--------------------------------
Fitxer: src/main/resources/hibernate.cfg.xml
- URL:      jdbc:mysql://localhost:3306/videojocs?createDatabaseIfNotExist=true
- Usuari:   root
- Contrasenya: la que hi ha al fitxer de configuració (canvieu-la si la vostra és una altra)

La base de dades "videojocs" es crea sola si no existeix. hbm2ddl.auto està en "create":
cada execució esborra i torna a crear les taules, així que s'ha d'executar sempre amb la BD buida.

Com executar-ho
---------------
- Demo: classe Main mostra el recorregut de demostració configurat al codi.
- Proves: classe ProvesManuals (a src/test/java) conté el recorregut base i casos avançats.
  Consulteu proves.md per distingir proves històriques executades dels casos avançats planificats.
  Els resultats marcats com a planificats encara no s'han executat; no hi ha captures atribuïdes a aquests casos.

Estructura
----------
- models/     entitats JPA (herència JOINED: Construccio > Assentament > Poblat/Ciutat, i Carretera); hi ha les dues @NamedQuery (Jugador.perPartida i Partida.perEstat)
- dao/        interfícies i implementacions (PartidaDao, JugadorDao) i ReglesJoc (comprovació comuna)
- util/       SessionManager i Missatges (tots els missatges d'error en constants)
- resources/dades/   CSV del tauler (vèrtexs, hexàgons, arestes)

Mètodes implementats
--------------------
PartidaDao: iniciarPartida, mostrarTauler, MostrarPartida, TirarDaus, PassarTorn, ComprovarVictoria, ExercitMesGran
JugadorDao: MostrarRecursos, ConstruirCarretera, ConstruirPoblat, MillorarACiutat, ComerciarBanca, CalcularPunts, MoureLladre, ComprarCarta, JugarCarta, VertexsDisponibles

Part avançada
-------------
Implementats: lladre (inclòs moviment i robatori), cartes de desenvolupament, ExercitMesGran i VertexsDisponibles.
Amb una tirada de 7, cada jugador amb més de 7 recursos descarta la meitat (arrodonint cap avall) abans de moure el lladre.

