/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package decisaocomatosdefala.model;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author tatia
 */
public class Impressao {

    /**
     * @return the dataHora
     */
    public Date getDataHora() {
        return dataHora;
    }

    /**
     * @param dataHora the dataHora to set
     */
    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public Impressao(String ticketId, String msgId, String verbo, String tipoVerbo,String mensagem,Date datahora) {
        this.ticketId = ticketId;
        this.msgId = msgId;
        this.verbo = verbo;
        this.tipoVerbo = tipoVerbo;
        this.mensagem = mensagem;
        this.dataHora = datahora;
    }

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
     * @return the msgId
     */
    public String getMsgId() {
        return msgId;
    }

    /**
     * @param msgId the msgId to set
     */
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    /**
     * @return the verbo
     */
    public String getVerbo() {
        return verbo;
    }

    /**
     * @param verbo the verbo to set
     */
    public void setVerbo(String verbo) {
        this.verbo = verbo;
    }

    /**
     * @return the tipoVerbo
     */
    public String getTipoVerbo() {
        return tipoVerbo;
    }

    /**
     * @param tipoVerbo the tipoVerbo to set
     */
    public void setTipoVerbo(String tipoVerbo) {
        this.tipoVerbo = tipoVerbo;
    }

    private String ticketId;
    private String msgId;
    private String verbo;
    private String mensagem;
    private String tipoVerbo;
    private Date dataHora;

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

}
