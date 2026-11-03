package dao;

import models.Partida;
import models.Jugador;
import models.enums.EstatPartida;

import java.util.List;

public interface IPartidaDao extends IGenericDao<Partida, Integer> {

    /*
     * Inicialitza una nova partida (estat PREPARACIO) amb entre 2 i 4 jugadors i en torna l'id.
     * L'ordre de torn és aleatori i el color s'assigna segons aquest ordre.
     * Carrega el tauler dels CSV de resources/dades/, reparteix terrenys i números a l'atzar
     * i col·loca el lladre al desert.
     * Llança IllegalArgumentException si el nombre de jugadors no és vàlid.
     * Retorna -1 si hi ha cap error durant la càrrega (rollback de tot).
     */
    int iniciarPartida(List<String> nomsJugadors);

    /*
     * Mostra tots els hexàgons de la partida (terreny, número i si hi ha el lladre) i, per cada un,
     * les construccions (poblats i ciutats) dels seus vèrtexs amb el propietari. Les carreteres no es mostren.
     * Amb un id inexistent mostra un missatge i no llança cap excepció.
     */
    void mostrarTauler(int idPartida);

    /*
     * Mostra l'estat de la partida (estat, ronda, jugador actiu i última tirada) i, per cada jugador,
     * els punts, poblats, ciutats, carreteres i el total de recursos (no el detall).
     * Amb un id inexistent mostra un missatge i no llança cap excepció.
     */
    void MostrarPartida(int idPartida);

    /*
     * Tira els dos daus per al jugador actiu i reparteix la producció: 1 recurs per poblat i 2 per ciutat
     * adjacents als hexàgons amb el número tirat (menys el del lladre). Amb un 7 no hi ha producció.
     * Es rebutja si la partida no existeix, no està EN_CURS o ja s'han tirat els daus aquest torn.
     */
    void TirarDaus(int idPartida);

    /*
     * Passa el torn. A PREPARACIO cal haver col·locat el poblat i la carretera, i l'ordre és de serp
     * (J1, J2, J3, J3, J2, J1); en acabar la partida passa a EN_CURS. A EN_CURS cal haver tirat els daus;
     * la ronda puja només quan el torn torna al jugador d'ordre 1.
     */
    void PassarTorn(int idPartida);

    /* Torna el líder únic amb almenys tres cavallers, o null si no n'hi ha. */
    Jugador ExercitMesGran(int idPartida);

    /*
     * Comprova si algun jugador ha arribat als punts per guanyar. Si és així, la partida passa a
     * FINALITZADA i es guarda el guanyador; a partir d'aleshores cap acció no s'accepta.
     */
    void ComprovarVictoria(int idPartida);

    /*
     * Mostra quantes partides hi ha en l'estat indicat i la ronda de cadascuna (consulta amb nom Partida.perEstat).
     */
    void mostrarPartidesPerEstat(EstatPartida estat);
}
