/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package decisaocomatosdefala.model;

import java.util.List;

/**
 *
 * @author tatia
 */
public class TicketsComMensagens {

    /**
     * @return the ticketId
     */
    public String getTicketId() {
        return ticketId;
    }

    /**
     * @param ticketId the ticketId to set
     */
    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * @return the mensagens
     */
    public List<Mensagem> getMensagens() {
        return mensagens;
    }

    /**
     * @param mensagens the mensagens to set
     */
    public void setMensagens(List<Mensagem> mensagens) {
        this.mensagens = mensagens;
    }
    
    private String ticketId;
    private List<Mensagem> mensagens;
}
