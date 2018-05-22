/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package decisaocomatosdefala.model;

import java.util.Date;


/**
 *
 * @author tatia
 */
public class Incidente {

    /**
     * @return the dataHora
     */
    public String getDataHora() {
        return dataHora;
    }

    /**
     * @param dataHora the dataHora to set
     */
    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    /**
     * @return the ticketId
     */
    public Integer getTicketId() {
        return ticketId;
    }

    /**
     * @param ticketId the ticketId to set
     */
    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * @return the msgId
     */
    public Integer getMsgId() {
        return msgId;
    }

    /**
     * @param msgId the msgId to set
     */
    public void setMsgId(Integer msgId) {
        this.msgId = msgId;
    }

    /**
     * @return the mensagem
     */
    public String getMensagem() {
        return mensagem;
    }

    /**
     * @param mensagem the mensagem to set
     */
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    
    private Integer ticketId;
    private Integer  msgId;
    private String  mensagem;
    private String dataHora;
    
    
}
