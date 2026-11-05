package models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import models.enums.EstatPartida;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NamedQuery(name = "Partida.perEstat",
        query = "from Partida p where p.estat = :estat order by p.id")
@Entity
@Table(name = "partida")
@DynamicInsert
public class Partida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estat", nullable = false, length = 15)
    private EstatPartida estat;

    @NotNull
    @Column(name = "dataInici", nullable = false)
    private LocalDateTime dataInici;

    @Min(value = 0, message = "La ronda no pot ser menor a cero")
    @Column(name = "ronda", nullable = false)
    private Integer ronda = 1;

    @Min(value = 0, message = "L'ultima tirada no pot ser menor a cero")
    @Max(value = 12, message = "L'ultima tirada no pot ser major a dotze")
    @Column(name = "ultimaTirada", nullable = false)
    private Integer ultimaTirada = 0;

    @Min(value = 0, message = "Els punts per guanyar no pot ser menor a cero")
    @Column(name = "puntsPerGuanyar", nullable = false)
    private Integer puntsPerGuanyar = 10;

    @Column(name = "dausTirats", nullable = false)
    private boolean dausTirats;

    @Column(name = "lladrePendent", nullable = false)
    private boolean lladrePendent;

    // LAZY: col·lecció; els DAO la recorren sempre dins de la sessió
    @OneToMany(mappedBy = "partida", fetch = FetchType.LAZY)
    @OrderBy("ordreTorn")
    private List<Jugador> jugadors = new ArrayList<>();

    // EAGER: una sola referència que es consulta a cada acció (de qui és el torn)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jugador_actiu_id")
    private Jugador jugadorActiu;

    // EAGER: una sola referència (null fins que algú guanya), igual que jugadorActiu
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jugador_guanyador_id")
    private Jugador jugadorGuanyador;
    
    // LAZY: 19 hexàgons amb les seves relacions; només es recorren dins de la sessió
    @OneToMany(mappedBy = "partida", fetch = FetchType.LAZY)
    @OrderBy("codi")
    private List<Hexagon> hexagons = new ArrayList<>();
    
    // LAZY: 54 vèrtexs per partida; només es recorren dins de la sessió
    @OneToMany(mappedBy = "partida", fetch = FetchType.LAZY)
    private List<Vertex> vertexs = new ArrayList<>();
    
    // LAZY: 72 arestes per partida; només es recorren dins de la sessió
    @OneToMany(mappedBy = "partida", fetch = FetchType.LAZY)
    private List<Aresta> arestas = new ArrayList<>();
    
    // LAZY: 21 cartes; només es consulten quan es compra una carta
    @OneToMany(mappedBy = "partida", fetch = FetchType.LAZY)
    private List<CartaDesenvolupament> pila = new ArrayList<>();

    // EAGER: una sola referència que es consulta a cada tirada (el lladre bloqueja la producció)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hexagon_lladre_id")
    private Hexagon lladre;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EstatPartida getEstat() {
        return estat;
    }

    public void setEstat(EstatPartida estat) {
        this.estat = estat;
    }

    public LocalDateTime getDataInici() {
        return dataInici;
    }

    public void setDataInici(LocalDateTime dataInici) {
        this.dataInici = dataInici;
    }

    public Integer getRonda() {
        return ronda;
    }

    public void setRonda(Integer ronda) {
        this.ronda = ronda;
    }

    public Integer getUltimaTirada() {
        return ultimaTirada;
    }

    public void setUltimaTirada(Integer ultimaTirada) {
        this.ultimaTirada = ultimaTirada;
    }

    public Integer getPuntsPerGuanyar() {
        return puntsPerGuanyar;
    }

    public void setPuntsPerGuanyar(Integer puntsPerGuanyar) {
        this.puntsPerGuanyar = puntsPerGuanyar;
    }

    public boolean isDausTirats() {
        return dausTirats;
    }

    public void setDausTirats(boolean dausTirats) {
        this.dausTirats = dausTirats;
    }

    public boolean isLladrePendent() {
        return lladrePendent;
    }

    public void setLladrePendent(boolean lladrePendent) {
        this.lladrePendent = lladrePendent;
    }

    public List<Jugador> getJugadors() {
        return jugadors;
    }

    public void setJugadors(List<Jugador> jugadors) {
        this.jugadors = jugadors;
    }

    public Jugador getJugadorActiu() {
        return jugadorActiu;
    }

    public void setJugadorActiu(Jugador jugadorActiu) {
        this.jugadorActiu = jugadorActiu;
    }

    public Jugador getJugadorGuanyador() {
        return jugadorGuanyador;
    }

    public void setJugadorGuanyador(Jugador jugadorGuanyador) {
        this.jugadorGuanyador = jugadorGuanyador;
    }

    public List<Hexagon> getHexagons() {
        return hexagons;
    }

    public void setHexagons(List<Hexagon> hexagons) {
        this.hexagons = hexagons;
    }

    public List<Vertex> getVertexs() {
        return vertexs;
    }

    public void setVertexs(List<Vertex> vertexs) {
        this.vertexs = vertexs;
    }

    public List<Aresta> getArestas() {
        return arestas;
    }

    public void setArestas(List<Aresta> arestas) {
        this.arestas = arestas;
    }

    public List<CartaDesenvolupament> getPila() {
        return pila;
    }

    public void setPila(List<CartaDesenvolupament> pila) {
        this.pila = pila;
    }

    public Hexagon getLladre() {
        return lladre;
    }

    public void setLladre(Hexagon lladre) {
        this.lladre = lladre;
    }

    // Iguals si tenen el mateix id; una entitat nova (id null) només és igual a ella mateixa.
    // Es fa servir getId() i no el camp perquè en un proxy els camps són buits.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Partida other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    // Constant: no pot canviar quan l'entitat rep l'id, ni dependre de la classe d'un proxy
    @Override
    public int hashCode() {
        return Partida.class.hashCode();
    }

    // Sense relacions ni col·leccions: podrien provocar LazyInitializationException o recursió infinita
    @Override
    public String toString() {
        return "Partida{" +
                "id=" + id +
                ", estat=" + estat +
                ", dataInici=" + dataInici +
                ", ronda=" + ronda +
                ", ultimaTirada=" + ultimaTirada +
                ", puntsPerGuanyar=" + puntsPerGuanyar +
                ", dausTirats=" + dausTirats +
                ", lladrePendent=" + lladrePendent +
                ", jugadorActiuId=" + (jugadorActiu != null ? jugadorActiu.getId() : null) +
                ", jugadorGuanyadorId=" + (jugadorGuanyador != null ? jugadorGuanyador.getId() : null) +
                ", lladreId=" + (lladre != null ? lladre.getId() : null) +
                '}';
    }
}
