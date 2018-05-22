/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Testes;

import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.impl.file.Morphology;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tatia
 */
public class TestandoPredominancia {

    public TestandoPredominancia() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void hello() {
        String mensagem = "Dear Regina Van Weerelt , we made intervention and analyze occurrences reported during the telephone contact where we ve"
                + " found that for one of the forwarded email accounts there was a typo , missing the  m  in  . com  . We have carried out the "
                + "e - mail referring to this correction . Ms . Regina informed us about e - mails that were sent indeterminate form of your account . "
                + "We have analyzed this case and we suspect that the email account used may have been invaded . We orientate Ms . "
                + "Regina to alter the password of the e - mail used . Due to unavailability of the user at the moment , "
                + "we are scheduling the completion of this call for today even the 13 : 30 h . Please be advised that your request registered "
                + "in the call 2015070310146046 will be dealt with soon . We are programming in order for the service to take place on :"
                + " DD / MM / YYYY at HH : MM pm . The team is responsible for : FOT The analyst responsible for your care is :"
                + " Francisco Dias Wait additional information soon . Thank you for having our help . Francisco Dias Field Operations Team "
                + "| [ 1 ] www . techmaster . com . br 21 2517 6000 2015070310146046 | [ 2 ] ";
//        System.out.println("Mensagem 1");
//        predominanciaDoAtoDeFala(mensagem);
//        mensagem = "Dear Marcell Vaz Fernandes , informed that his request registered in the call 2015070310146063 will be dealt with soon . Awaiting further information soon . Thank you for having our help . Ruth Carvalho IT Services Team | [ 1 ] www . techmaster . com . br 21 2517 6000 2015070310146063 | [ 2 ] ";
//        System.out.println("Mensagem 2");
//        predominanciaDoAtoDeFala(mensagem);
        mensagem = "S requires H to A if S expresses";
        predominanciaDoAtoDeFala(mensagem);
    }

    public Boolean atoDeFalaInformative(String mensagem) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechAct\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        for (int i = 0; i < tokens.length; i++) {
            System.out.println("tag=" + tags[i] + "- [" + tokens[i] + "]");
        }

        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG"};
        String[] objeto = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        String[] pronome = {"NNP", "NNPS"};
        String[] informatives = {"announce", "apprise", "disclose", "inform", "insist", "notify", "point out", "report", "reveal", "tell"};
        Boolean achouInformative = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouObjeto = false;
        Boolean achouPronome = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
                    System.out.println("Sujeito=" + sujeito[s]);
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouSujeito = true;
                    break;
                }
            }
            if (achouSujeito == true) {
                break;
            }
        }

        for (int i = passaValor; i < tokens.length; i++) {
            for (int v = 0; v < verbo.length; v++) {
                if (tags[i].equals(verbo[v])) {
                    for (int in = 0; in < informatives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(informatives[in]))) {
                            System.out.println("Verbo=" + verbo[v]);
                            modelo = new Modelo();
                            modelo.setConteudo(tokens[i]);
                            modelo.setTag(tags[i]);
                            modelo.setPosicao(i);
                            respostas.add(modelo);
                            passaValor = i;
                            achouVerbo = true;
                            break;
                        }
                    }
                }
            }
            if (achouVerbo == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < objeto.length; o++) {
                if (tags[i].equals(objeto[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouObjeto = true;
                    break;
                }
            }
            if (achouObjeto == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int p = 0; p < pronome.length; p++) {
                if (tags[i].equals(pronome[p])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPronome = true;
                    break;
                }
            }
            if (achouPronome == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouObjeto == true && achouPronome
                == true) {
            achouInformative = true;
        }
        return achouInformative;
    }

    public static String convertendoParaInfinitivo(String verb) {
        System.setProperty("wordnet.database.dir", "C:\\SpeechAct\\test\\dict\\");
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Morphology id = Morphology.getInstance();
        String[] arr = id.getBaseFormCandidates(verb, SynsetType.VERB);
        String verbo = "";
        for (String a : arr) {
            verbo = a;
        }
        return verbo;
    }

    public void predominanciaDoAtoDeFala(String mensagem) {
        Boolean informative = atoDeFalaInformative(mensagem);
        if (informative == true) {
            System.out.println("É predominante Informative");
        }
    }

}
