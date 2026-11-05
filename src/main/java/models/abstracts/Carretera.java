package models.abstracts;

import jakarta.persistence.*;
import models.Aresta;

@Entity
public class Carretera extends Construccio {

    // EAGER: una sola referència; una carretera sempre es comprova amb la seva aresta
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aresta_id", unique = true, nullable = false)
    private Aresta aresta;

    public Aresta getAresta() {
        return aresta;
    }

    public void setAresta(Aresta aresta) {
        this.aresta = aresta;
        if (aresta != null) {
            aresta.setCarretera(this);
        }
    }

    @Override
    public String toString() {
        return "Carretera{" +
                "arestaId=" + (aresta != null ? aresta.getId() : null) +
                '}';
    }
}
