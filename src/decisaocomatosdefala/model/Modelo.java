/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package decisaocomatosdefala.model;


/**
 *
 * @author tatia
 */
public class Modelo {

    /**
     * @return the posicao
     */
    public Integer getPosicao() {
        return posicao;
    }

    /**
     * @param posicao the posicao to set
     */
    public void setPosicao(Integer posicao) {
        this.posicao = posicao;
    }

    /**
     * @return the conteudo
     */
    public String getConteudo() {
        return conteudo;
    }

    /**
     * @param conteudo the conteudo to set
     */
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    /**
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * @param tag the tag to set
     */
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    private Integer posicao;
    private String conteudo;
    private String tag;
    
}
