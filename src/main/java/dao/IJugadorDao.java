package dao;

import models.Jugador;
import models.Vertex;
import java.util.List;
import models.enums.TipusRecurs;

public interface IJugadorDao extends IGenericDao<Jugador, Integer> {

    /*
     * Mostra els 5 tipus de recurs del jugador amb la quantitat de cadascun.
     * Amb un id inexistent mostra un missatge i no llança cap excepció.
     */
    void MostrarRecursos(int idJugador);

    /*
     * Construeix una carretera a l'aresta indicada. A PREPARACIO és gratuïta i ha de tocar el poblat
     * acabat de col·locar; a EN_CURS costa 1 FUSTA + 1 MAO, ha d'estar connectada i cal haver tirat els daus.
     * Si es rebutja, mostra el motiu i no canvia res a la BD.
     */
    void ConstruirCarretera(int idJugador, int idAresta);

    /*
     * Construeix un poblat al vèrtex indicat, respectant la regla de la distància. A PREPARACIO és gratuït
     * (el segon dona 1 recurs per hexàgon no desert); a EN_CURS costa 1 FUSTA + 1 MAO + 1 LLANA + 1 BLAT
     * i ha d'estar connectat a una carretera pròpia. Comprova la victòria en acabar.
     */
    void ConstruirPoblat(int idJugador, int idVertex);

    /*
     * Substitueix un poblat propi del vèrtex per una ciutat (2 BLAT + 3 MINERAL). Només a EN_CURS
     * i havent tirat els daus. Comprova la victòria en acabar.
     */
    void MillorarACiutat(int idJugador, int idVertex);

    /*
     * Comercia amb la banca: el jugador dona 4 recursos del tipus "dona" i rep 1 del tipus "rep".
     * Es rebutja si els tipus són iguals, si té menys de 4 o fora de torn.
     */
    void ComerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep);

    /* Mou el lladre pendent a un altre hexàgon i roba un recurs d'una víctima adjacent vàlida. */
    void MoureLladre(int idJugador, int idHexagon, Integer idJugadorVictima);

    /* Compra una carta aleatòria per 1 BLAT, 1 LLANA i 1 MINERAL, si la pila no és buida. */
    void ComprarCarta(int idJugador);

    /* Juga una carta adquirida en una ronda anterior; MONOPOLI requereix un tipus de recurs. */
    void JugarCarta(int idJugador, int idCarta, TipusRecurs recurs);

    /* Consulta els vèrtexs lliures que compleixen distància i, fora de preparació, connexió. */
    List<Vertex> VertexsDisponibles(int idJugador);

    /*
     * Torna els punts del jugador: poblat = 1, ciutat = 2, carta PUNT_VICTORIA = 1 i exèrcit més gran = 2.
     * Torna 0 amb un id inexistent.
     */
    int CalcularPunts(int idJugador);
}
