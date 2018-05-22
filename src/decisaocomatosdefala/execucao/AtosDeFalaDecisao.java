/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package decisaocomatosdefala.execucao;

import decisaocomatosdefala.model.Impressao;
import decisaocomatosdefala.model.Mensagem;
import decisaocomatosdefala.model.Modelo;
import decisaocomatosdefala.model.TicketsComMensagens;
import decisaocomatosdefala.model.Verbo;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.impl.file.Morphology;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.Span;
import static opennlp.tools.util.Span.spansToStrings;
import opennlp.tools.util.StringUtil;
import org.junit.Test;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;

/**
 *
 * @author tatia
 */
public class AtosDeFalaDecisao {

    private static String[] defaultStopWords = {"#", "$", "%", "\"", "\'"};
    private static HashSet stopWords = new HashSet();

    public AtosDeFalaDecisao() {
        stopWords.addAll(Arrays.asList(defaultStopWords));
    }

    public static String[] removeStopWords(String[] words) {
        ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(words));
        for (int i = 0; i < tokens.size(); i++) {
            if (stopWords.contains(tokens.get(i))) {
                tokens.remove(i);
            }
        }
        return (String[]) tokens.toArray(
                new String[tokens.size()]);
    }

    public static void main(String args[]) throws IOException, ParseException {
        execucaoDoArquivo("C:\\SpeechActMiner\\arquivos\\LogMessage.csv");
    }

    public static Date converterStringParaDate(String data) throws ParseException {
        TimeZone tz = TimeZone.getTimeZone("Asia/Calcutta");
        Calendar cal = Calendar.getInstance(tz);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setCalendar(cal);
        cal.setTime(sdf.parse(data));
        Date date = cal.getTime();
        return date;
    }

    public TicketsComMensagens buscarPontosDeDecisao(TicketsComMensagens ticket) {
        TicketsComMensagens ticketPonto = new TicketsComMensagens();
        ticketPonto = buscandoVerbosEmMensagens(ticket);
        return ticketPonto;
    }

    public static List<TicketsComMensagens> leituraDoArquivoCSV(String caminho) throws FileNotFoundException, IOException, ParseException {
        BufferedReader br = null;
        caminho = new File(caminho).getCanonicalPath();
        br = new BufferedReader(new FileReader(caminho));
        String linha = br.readLine();
        String csvDivisor = ";";
        String[] colunas = linha.split(csvDivisor);
        TicketsComMensagens ticket = new TicketsComMensagens();
        ticket.setTicketId((colunas[1]));
        Mensagem mensagem = new Mensagem();
        mensagem.setMsgId((colunas[0]));
        mensagem.setMensagem(removendoCaracter(colunas[2]));
        List<Mensagem> mensagens = new ArrayList<Mensagem>();
        mensagens.add(mensagem);
        List<TicketsComMensagens> tickets = new ArrayList<TicketsComMensagens>();
        int tam = tickets.size();
        int i = 0;
        TicketsComMensagens ticketPonto = null;
        while ((linha = br.readLine()) != null) {
            try {
                colunas = linha.split(csvDivisor);
                if (!colunas[1].equals(ticket.getTicketId().toString())) {
                    ticket.setMensagens(mensagens);
                    tickets.add(ticket);
                    ticket = new TicketsComMensagens();
                    ticket.setTicketId((colunas[1]));
                    mensagens = new ArrayList<Mensagem>();
                }
                mensagem = new Mensagem();
                mensagem.setMsgId((colunas[0]));
                mensagem.setMensagem(removendoCaracter(colunas[2]));
                mensagens.add(mensagem);
            } catch (Exception e) {

            }
        }
        ticket.setMensagens(mensagens);
        tickets.add(ticket);
        return tickets;
    }

    public static void execucaoDoArquivo(String caminho) throws IOException, FileNotFoundException, ParseException {
        //Leitura do Arquivo
        List<TicketsComMensagens> tickets = leituraDoArquivoCSV(caminho);
        //Limpeza das Mensagens
        List<TicketsComMensagens> ticketLimpos = new ArrayList<TicketsComMensagens>();
        List<Mensagem> mensagens = new ArrayList<Mensagem>();
        TicketsComMensagens ticketLimpo = null;
        if (tickets.isEmpty() == false) {
            for (TicketsComMensagens ticket : tickets) {
                ticketLimpo = new TicketsComMensagens();
                ticketLimpo = ticket;
                Mensagem mensagemLimpa = null;
                for (Mensagem msg : ticket.getMensagens()) {
                    mensagemLimpa = new Mensagem();
                    mensagemLimpa = msg;
                    mensagemLimpa.setMensagem(removendoCaracter(mensagemLimpa.getMensagem()));
                    mensagens.add(mensagemLimpa);
                }
                ticketLimpo.setMensagens(mensagens);
                ticketLimpos.add(ticketLimpo);

                mensagens = new ArrayList<Mensagem>();
            }
        }

        List<Impressao> decisoesEncontradas = new ArrayList<>();
        List<TicketsComMensagens> ticketsComVerbos = buscandoVerbosEmMensagens(ticketLimpos);
        //Listar somente pontos de decisão

        for (TicketsComMensagens ticket : ticketsComVerbos) {
            for (Mensagem msg : ticket.getMensagens()) {
                for (Verbo verbo : msg.getVerbos()) {
                    Impressao impressao = null;
                    if (verbo.getTipoVerbo().equals("decision")) {
                        impressao = new Impressao(ticket.getTicketId(), msg.getMsgId(), verbo.getVerbo(), verbo.getTipoVerbo(), msg.getMensagem(), msg.getDatahora());
                        decisoesEncontradas.add(impressao);
                    }
                }
            }
        }

        List<Impressao> mensagensAnteriores = new ArrayList<>();

        int msgid = 0;
        Impressao impressao = null;
        for (TicketsComMensagens ticket : ticketsComVerbos) {
            for (Mensagem mensagem : ticket.getMensagens()) {
                Boolean achou = false;
                Verbo verboAchado = null;
                Mensagem mensagemAchada = null;
                for (Impressao impDecisao : decisoesEncontradas) {
                    if (impDecisao.getTicketId().toString().equals(ticket.getTicketId().toString())) {
                        if (mensagem.getMsgId().toString().equals(impDecisao.getMsgId())) {
                        } else {
                            if (Long.parseLong(mensagem.getMsgId()) < Long.parseLong(impDecisao.getMsgId())) {
                                for (Verbo verbo : mensagem.getVerbos()) {
                                    if (!verbo.getTipoVerbo().equals("decision")) {
                                        achou = true;
                                        verboAchado = new Verbo(verbo.getTipoVerbo(), verbo.getVerbo());
                                        mensagemAchada = new Mensagem();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (achou == true) {
                    impressao = new Impressao(ticket.getTicketId(), mensagem.getMsgId(), verboAchado.getVerbo(), verboAchado.getTipoVerbo(), mensagem.getMensagem(), mensagem.getDatahora());
                    mensagensAnteriores.add(impressao);
                }

            }
        }
        FileWriter writer = new FileWriter("C:\\SpeechActMiner\\arquivos\\resultadoLog.csv");

        System.out.println("PONTOS DE DECISÃO");
        System.out.println("==========================================================================");
        for (Impressao imp : decisoesEncontradas) {
            System.out.println(imp.getTicketId() + ";" + imp.getMsgId() + ";" + imp.getVerbo() + ";" + imp.getTipoVerbo() + ";" + imp.getMensagem());
            writer.append(imp.getTicketId() + ";" + imp.getMsgId() + ";" + imp.getVerbo() + ";" + imp.getTipoVerbo() + ";" + imp.getMensagem() + "\";");
            writer.append(System.lineSeparator());
        }
        System.out.println("Mensagens anteriores =" + mensagensAnteriores.size());
        System.out.println("==========================================================================");
        for (Impressao imp : mensagensAnteriores) {
            System.out.println(imp.getTicketId() + ";" + imp.getMsgId() + ";" + imp.getTipoVerbo() + ";" + imp.getVerbo() + ";" + imp.getMensagem());
            writer.append(imp.getTicketId() + ";" + imp.getMsgId() + ";" + imp.getTipoVerbo() + ";" + imp.getVerbo() + ";" + imp.getMensagem() + "\";");
            writer.append(System.lineSeparator());
        }
        System.out.println("==========================================================================");
        writer.flush();
        writer.close();

    }

    public static List<TicketsComMensagens> buscandoVerbosEmMensagens(List<TicketsComMensagens> tickets) {
        List<TicketsComMensagens> ticketsComVerbos = new ArrayList<>();
        List<Mensagem> mensagensComVerbos = new ArrayList<>();
        Mensagem msgNovo = null;
        TicketsComMensagens ticketNovo = null;
        for (TicketsComMensagens ticket : tickets) {
            ticketNovo = new TicketsComMensagens();
            ticketNovo.setTicketId(ticket.getTicketId());
            for (Mensagem msg : ticket.getMensagens()) {
                List<Verbo> verbos = new ArrayList<>();
                verbos = buscandoVerbos(msg.getMensagem());
                msgNovo = new Mensagem();
                msgNovo.setMensagem(msg.getMensagem());
                msgNovo.setMsgId(msg.getMsgId());
                msgNovo.setVerbos(verbos);
                mensagensComVerbos.add(msgNovo);
            }
            ticketNovo.setMensagens(mensagensComVerbos);
            ticketsComVerbos.add(ticketNovo);
            mensagensComVerbos = new ArrayList<>();
        }
        return ticketsComVerbos;
    }

    public static TicketsComMensagens buscandoVerbosEmMensagens(TicketsComMensagens ticket) {
        List<Mensagem> mensagensComVerbos = new ArrayList<>();
        Mensagem msgNovo = null;
        TicketsComMensagens ticketNovo = null;
        ticketNovo = new TicketsComMensagens();
        ticketNovo.setTicketId(ticket.getTicketId());
        for (Mensagem msg : ticket.getMensagens()) {
            List<Verbo> verbos = new ArrayList<>();
            verbos = buscandoVerbos(msg.getMensagem());
            msgNovo = new Mensagem();
            msgNovo.setMensagem(msg.getMensagem());
            msgNovo.setMsgId(msg.getMsgId());
            msgNovo.setVerbos(verbos);
            mensagensComVerbos.add(msgNovo);
        }
        ticketNovo.setMensagens(mensagensComVerbos);
        return ticketNovo;
    }

    public static List<Verbo> buscandoVerbos(String mensagem) {
        List<Verbo> verbos = new ArrayList<Verbo>();
        String[] assertive = {"affirm", "allege", "assert", "aver", "avow", "claim", "declare", "indicate", "maintain", "propound", "say", "state", "submit"};
        String[] predictive = {"forecast", "predict", "prophesy"};
        String[] retrodictives = {"recount"};
        String[] descriptives = {"call", "categorize", "characterize", "classify", "date", "describe", "diagnose", "evaluate", "grade", "identify", "portray", "rank"};
        String[] ascriptives = {"ascribe", "attribute", "predicate"};
        String[] informatives = {"announce", "apprise", "disclose", "inform", "insist", "notify", "point out", "report", "reveal", "tell"};
        String[] confirmatives = {"appraise", "assess", "bear witness", "certify", "conclude", "confirm", "corroborate", "find", "judge", "substantiate", "testif", "validate", "verif", "vouch for"};
        String[] concessives = {"acknowledge", "admit", "agree", "concede", "concur"};
        String[] retractives = {"abjure", "correct", "deny", "disavow", "disclaim", "disown", "recant", "renounce", "repudiate", "retract", "take back", "withdraw"};
        String[] assentives = {"accept", "assent", "concur"};
        String[] dissentives = {"differ", "disagree", "dissent", "reject"};
        String[] disputatives = {"demur", "dispute", "object", "protest"};
        String[] responsives = {"answer", "reply", "respond", "retort"};
        String[] suggestives = {"conjecture", "guess", "speculate", "suggest"};
        String[] suppositives = {"assume", "hypothesize", "postulate", "stipulate", "suppose", "theorize"};
        String[] requestives = {"ask", "beg", "beseech", "implore", "insist", "invite", "petition", "plead", "pray", "request", "solicit", "summon", "supplicate", "urge"};
        String[] questions = {"inquire", "interrogate", "query", "question", "quiz"};
        String[] requirements = {"bid", "charge", "command", "demand", "dictate", "direct", "enjoin", "instruct", "order", "prescribe", "require"};
        String[] prohibitives = {"forbid", "prohibit", "proscribe", "restrict"};
        String[] permissives = {"allow", "authorize", "bless", "consent to", "dismiss", "excuse", "exempt", "forgive", "grant", "license", "pardon", "release", "sanction"};
        String[] advisories = {"admonish", "advise", "caution", "counsel", "propose", "recommend", "suggest", "urge", "warn"};
        String[] promises = {"promise", "swear", "vow"};
        String[] offers = {"offer"};
        String[] decision = {"close", "complete", "normalized", "solved", "agreed", "choosen", "conclude", "determine", "elect", "end", "establish", "rule", "select", "set", "vote", "detail", "diagnostic", "discrete", "procedure"};
        Verbo verboItem = null;
        String verbo = mensagem.toLowerCase().trim();
        for (String decisao : decision) {
            String decisao1 = decisao;
            decisao = convertendoParaInfinitivo(decisao);
            if (decisao.equals("")) {
                decisao = decisao1;
            }
            if (verbo.contains(decisao.trim().toLowerCase())) {
                verboItem = new Verbo(decisao1, "decision");
                verbos.add(verboItem);
            }
        }
        for (String assertiva : assertive) {
            String assertiva1 = assertiva;
            assertiva = convertendoParaInfinitivo(assertiva);
            if (assertiva.equals("")) {
                assertiva = assertiva1;
            }
            if (verbo.contains(assertiva.trim().toLowerCase()) && Objects.equals(atoDeFalaAssertive(mensagem, assertive), Boolean.TRUE)) {
                verboItem = new Verbo(assertiva1, "assertive");
                verbos.add(verboItem);
            }
        }
        for (String predictiva : predictive) {
            String predictiva1 = predictiva;
            predictiva = convertendoParaInfinitivo(predictiva);
            if (predictiva.equals("")) {
                predictiva = predictiva1;
            }
            if (verbo.contains(predictiva.trim().toLowerCase()) && Objects.equals(atoDeFalaPredictive(mensagem, predictive), Boolean.TRUE)) {
                verboItem = new Verbo(predictiva1, "predictive");
                verbos.add(verboItem);
            }
        }
        for (String retrodictive : retrodictives) {
            String retrodictive1 = retrodictive;
            retrodictive = convertendoParaInfinitivo(retrodictive);
            if (retrodictive.equals("")) {
                retrodictive = retrodictive1;
            }
            if (verbo.contains(retrodictive.trim().toLowerCase()) && Objects.equals(atoDeFalaRetrodictive(mensagem, retrodictives), Boolean.TRUE)) {
                verboItem = new Verbo(retrodictive1, "retrodictive");
                verbos.add(verboItem);
            }
        }
        for (String descriptive : descriptives) {
            String descriptive1 = descriptive;
            descriptive = convertendoParaInfinitivo(descriptive);
            if (descriptive.equals("")) {
                descriptive = descriptive1;
            }
            if (verbo.contains(descriptive.trim().toLowerCase()) && Objects.equals(atoDeFalaDescriptive(mensagem, descriptives), Boolean.TRUE)) {
                verboItem = new Verbo(descriptive1, "descriptive");
                verbos.add(verboItem);
            }
        }
        for (String ascriptive : ascriptives) {
            String ascriptive1 = ascriptive;
            ascriptive = convertendoParaInfinitivo(ascriptive);
            if (ascriptive.equals("")) {
                ascriptive = ascriptive1;
            }
            if (verbo.contains(ascriptive.trim().toLowerCase()) && Objects.equals(atoDeFalaAscriptives(mensagem, ascriptives), Boolean.TRUE)) {
                verboItem = new Verbo(ascriptive1, "ascriptive");
                verbos.add(verboItem);
            }
        }
        for (String informative : informatives) {
            String informative1 = informative;
            informative = convertendoParaInfinitivo(informative);
            if (informative.equals("")) {
                informative = informative1;
            }
            if (verbo.contains(informative.trim().toLowerCase()) && Objects.equals(atoDeFalaInformative(mensagem, informatives), Boolean.TRUE)) {
                verboItem = new Verbo(informative1, "informative");
                verbos.add(verboItem);
            }
        }
        for (String confirmative : confirmatives) {
            String confirmative1 = confirmative;
            confirmative = convertendoParaInfinitivo(confirmative);
            if (confirmative.equals("")) {
                confirmative = confirmative1;
            }
            if (verbo.contains(confirmative.trim().toLowerCase()) && Objects.equals(atoDeFalaConfirmative(mensagem, confirmatives), Boolean.TRUE)) {
                verboItem = new Verbo(confirmative1, "confirmative");
                verbos.add(verboItem);
            }
        }
        for (String concessive : concessives) {
            String concessive1 = concessive;
            concessive = convertendoParaInfinitivo(concessive);
            if (concessive.equals("")) {
                concessive = concessive1;
            }
            if (verbo.contains(concessive.trim().toLowerCase()) && Objects.equals(atoDeFalaConcessive(mensagem, concessives), Boolean.TRUE)) {
                verboItem = new Verbo(concessive1, "concessive");
                verbos.add(verboItem);
            }
        }
        for (String retractive : retractives) {
            String retractive1 = retractive;
            retractive = convertendoParaInfinitivo(retractive);
            if (retractive.equals("")) {
                retractive = retractive1;
            }
            if (verbo.contains(retractive.trim().toLowerCase()) && Objects.equals(atoDeFalaRetractive(mensagem, retractives), Boolean.TRUE)) {
                verboItem = new Verbo(retractive1, "retractive");
                verbos.add(verboItem);
            }
        }
        for (String assentive : assentives) {
            String assentive1 = assentive;
            assentive = convertendoParaInfinitivo(assentive);
            if (assentive.equals("")) {
                assentive = assentive1;
            }
            if (verbo.contains(assentive.trim().toLowerCase()) && Objects.equals(atoDeFalaAssentive(mensagem, assentives), Boolean.TRUE)) {
                verboItem = new Verbo(assentive1, "assentive");
                verbos.add(verboItem);
            }
        }
        for (String dissentive : dissentives) {
            String dissentive1 = dissentive;
            dissentive = convertendoParaInfinitivo(dissentive);
            if (dissentive.equals("")) {
                dissentive = dissentive1;
            }
            if (verbo.contains(dissentive.trim().toLowerCase()) && Objects.equals(atoDeFalaDissentive(mensagem, dissentives), Boolean.TRUE)) {
                verboItem = new Verbo(dissentive1, "dissentive");
                verbos.add(verboItem);
            }
        }
        for (String disputative : disputatives) {
            String disputative1 = disputative;
            disputative = convertendoParaInfinitivo(disputative);
            if (disputative.equals("")) {
                disputative = disputative1;
            }
            if (verbo.contains(disputative.trim().toLowerCase()) && Objects.equals(atoDeFalaDisputative(mensagem, disputatives), Boolean.TRUE)) {
                verboItem = new Verbo(disputative1, "disputative");
                verbos.add(verboItem);
            }
        }
        for (String responsive : responsives) {
            String responsive1 = responsive;
            responsive = convertendoParaInfinitivo(responsive);
            if (responsive.equals("")) {
                responsive = responsive1;
            }
            if (verbo.contains(responsive.trim().toLowerCase()) && Objects.equals(atoDeFalaResponsives(mensagem, responsives), Boolean.TRUE)) {
                verboItem = new Verbo(responsive1, "responsive");
                verbos.add(verboItem);
            }
        }
        for (String suggestive : suggestives) {
            String suggestive1 = suggestive;
            suggestive = convertendoParaInfinitivo(suggestive);
            if (suggestive.equals("")) {
                suggestive = suggestive1;
            }
            if (verbo.contains(suggestive.trim().toLowerCase()) && Objects.equals(atoDeFalaSuggestives(mensagem, suggestives), Boolean.TRUE)) {
                verboItem = new Verbo(suggestive1, "suggestive");
                verbos.add(verboItem);
            }
        }
        for (String suppositive : suppositives) {
            String suppositive1 = suppositive;
            suppositive = convertendoParaInfinitivo(suppositive);
            if (suppositive.equals("")) {
                suppositive = suppositive1;
            }
            if (verbo.contains(suppositive.trim().toLowerCase()) && Objects.equals(atoDeFalaSuppositive(mensagem, suppositives), Boolean.TRUE)) {
                verboItem = new Verbo(suppositive1, "suppositive");
                verbos.add(verboItem);
            }
        }
        for (String requestive : requestives) {
            String requestive1 = requestive;
            requestive = convertendoParaInfinitivo(requestive);
            if (requestive.equals("")) {
                requestive = requestive1;
            }
            if (verbo.contains(requestive.trim().toLowerCase()) && Objects.equals(atoDeFalaRequestive(mensagem, requestives), Boolean.TRUE)) {
                verboItem = new Verbo(requestive1, "requestive");
                verbos.add(verboItem);
            }
        }
        for (String question : questions) {
            String question1 = question;
            question = convertendoParaInfinitivo(question);
            if (question.equals("")) {
                question = question1;
            }
            if (verbo.contains(question.trim().toLowerCase()) && Objects.equals(atoDeFalaQuestions(mensagem, questions), Boolean.TRUE)) {
                verboItem = new Verbo(question1, "question");
                verbos.add(verboItem);
            }
        }
        for (String requirement : requirements) {
            String requirement1 = requirement;
            requirement = convertendoParaInfinitivo(requirement);
            if (requirement.equals("")) {
                requirement = requirement1;
            }
            if (verbo.contains(requirement.trim().toLowerCase()) && Objects.equals(atoDeFalaRequirements(mensagem, requirements), Boolean.TRUE)) {
                verboItem = new Verbo(requirement1, "requirement");
                verbos.add(verboItem);
            }
        }
        for (String prohibitive : prohibitives) {
            String prohibitive1 = prohibitive;
            prohibitive = convertendoParaInfinitivo(prohibitive);
            if (prohibitive.equals("")) {
                prohibitive = prohibitive1;
            }
            if (verbo.contains(prohibitive.trim().toLowerCase()) && Objects.equals(atoDeFalaProhibitive(mensagem, prohibitives), Boolean.TRUE)) {
                verboItem = new Verbo(prohibitive1, "prohibitive");
                verbos.add(verboItem);
            }
        }
        for (String permissive : permissives) {
            String permissive1 = permissive;
            permissive = convertendoParaInfinitivo(permissive);
            if (permissive.equals("")) {
                permissive = permissive1;
            }
            if (verbo.contains(permissive.trim().toLowerCase()) && Objects.equals(atoDeFalaPermissive(mensagem, permissives), Boolean.TRUE)) {
                verboItem = new Verbo(permissive1, "permissive");
                verbos.add(verboItem);
            }
        }
        for (String advisorie : advisories) {
            String advisorie1 = advisorie;
            advisorie = convertendoParaInfinitivo(advisorie);
            if (advisorie.equals("")) {
                advisorie = advisorie1;
            }
            if (verbo.contains(advisorie.trim().toLowerCase()) && Objects.equals(atoDeFalaAdvisories(mensagem, advisories), Boolean.TRUE)) {
                verboItem = new Verbo(advisorie1, "advisories");
                verbos.add(verboItem);
            }
        }
        for (String promise : promises) {
            String promise1 = promise;
            promise = convertendoParaInfinitivo(promise);
            if (promise.equals("")) {
                promise = promise1;
            }
            if (verbo.contains(promise.trim().toLowerCase()) && Objects.equals(atoDeFalaPromisse(mensagem, promises), Boolean.TRUE)) {
                verboItem = new Verbo(promise1, "promises");
                verbos.add(verboItem);
            }
        }
        for (String offer : offers) {
            String offer1 = offer;
            offer = convertendoParaInfinitivo(offer);
            if (offer.equals("")) {
                offer = offer1;
            }
            if (verbo.contains(offer.trim().toLowerCase()) && Objects.equals(atoDeFalaOffer(mensagem, offers), Boolean.TRUE)) {
                verboItem = new Verbo(offer1, "offers");
                verbos.add(verboItem);
            }
        }

        return verbos;
    }

    public static String convertendoParaInfinitivo(String verb) {
        System.setProperty("wordnet.database.dir", "C:\\SpeechActMiner\\test\\dict\\");
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Morphology id = Morphology.getInstance();
        String[] arr = id.getBaseFormCandidates(verb, SynsetType.VERB);
        String verbo = "";
        for (String a : arr) {
            verbo = a;
        }
        return verbo;
    }

    public static String[] tokenize(String s) {
        return spansToStrings(tokenizePos(s), s);
    }

    public static Span[] tokenizePos(String s) {
        CharacterEnum charType = CharacterEnum.WHITESPACE;
        CharacterEnum state = charType;

        List<Span> tokens = new ArrayList<>();
        int sl = s.length();
        int start = -1;
        char pc = 0;
        for (int ci = 0; ci < sl; ci++) {
            char c = s.charAt(ci);
            if (StringUtil.isWhitespace(c)) {
                charType = CharacterEnum.WHITESPACE;
            } else if (Character.isLetter(c)) {
                charType = CharacterEnum.ALPHABETIC;
            } else if (Character.isDigit(c)) {
                charType = CharacterEnum.NUMERIC;
            } else {
                charType = CharacterEnum.OTHER;
            }
            if (state == CharacterEnum.WHITESPACE) {
                if (charType != CharacterEnum.WHITESPACE) {
                    start = ci;
                }
            } else {
                if (charType != state || charType == CharacterEnum.OTHER && c != pc) {
                    tokens.add(new Span(start, ci));
                    start = ci;
                }
            }
            state = charType;
            pc = c;
        }
        if (charType != CharacterEnum.WHITESPACE) {
            tokens.add(new Span(start, sl));
        }
        return tokens.toArray(new Span[tokens.size()]);

    }

    static class CharacterEnum {

        static final CharacterEnum WHITESPACE = new CharacterEnum("whitespace");
        static final CharacterEnum ALPHABETIC = new CharacterEnum("alphabetic");
        static final CharacterEnum NUMERIC = new CharacterEnum("numeric");
        static final CharacterEnum OTHER = new CharacterEnum("other");

        private String name;

        private CharacterEnum(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static String removendoCaracter(String paragraph) {
        SimpleTokenizer simpleTokenizer = SimpleTokenizer.INSTANCE;
        String tokens[] = simpleTokenizer.tokenize(paragraph);
        String list[] = removeStopWords(tokens);
        paragraph = "";
        for (String word : list) {
            paragraph += word + " ";
        }
        return paragraph;
    }

    public List<Impressao> leituraDeCsvParaImpressao(String caminho) throws FileNotFoundException, IOException {
        List<Impressao> impressoes = new ArrayList<>();
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(caminho));
        String linha = "";
        String csvDivisor = ";";
        Impressao impressao = null;
        while ((linha = br.readLine()) != null) {
            try {
                String[] colunas = linha.split(csvDivisor);
                String parte1Data = colunas[5].replaceAll("\"", "").substring(0, 16);
                String parte2Data = colunas[5].replaceAll("\"", "").substring(17, 20);
                String data = parte1Data + ":00.0" + parte2Data;
                Date dataHora = (converterStringParaDate(data));
                impressao = new Impressao(colunas[0].replaceAll("\"", ""), colunas[1].replaceAll("\"", ""), colunas[2].replaceAll("\"", ""), colunas[3].replaceAll("\"", ""), colunas[4].replaceAll("\"", ""), dataHora);
                impressoes.add(impressao);
            } catch (Exception e) {

            }

        }
        return impressoes;
    }

    public static Boolean atoDeFalaInformative(String mensagem, String[] informatives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] objeto = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        String[] pronome = {"NNP", "NNPS"};
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

//         requestives = In uttering e, S requests H to A if S expresses:
//i. the desire that H do A, and
//ii. the intention that H do A because (at least partly) of S's desire.    
    public static Boolean atoDeFalaRequestive(String mensagem, String[] requestives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouRequestive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {

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
                    for (int in = 0; in < requestives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(requestives[in]))) {

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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {

                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true) {
            achouRequestive = true;
        }
        return achouRequestive;
    }

    /*
    Assertives (simple): (affirm, allege, assert, aver, avow, claim, declare,
deny (assert ... not), indicate, maintain, propound, say, state, submit)
In uttering e, S asserts that P if S expresses:
i. the belief that P, and
ii. the intention that H believe that P.
     */
    public static Boolean atoDeFalaAssertive(String mensagem, String[] assertives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouAssertive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
                    for (int in = 0; in < assertives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(assertives[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    passaValor = i;
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true) {
            achouAssertive = true;
        }
        return achouAssertive;
    }

    /*
    Predictives: (forecast, predict, prophesy)
In uttering e, S predicts that P if S expresses:
i. the belief that it will be the case that P, and
ii. the intention that H believe that it will be the case that P.
     */
    public static Boolean atoDeFalaPredictive(String mensagem, String[] predictives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] preposicao = {"IN", "TO"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouPredictive = false;
        Boolean achouSujeito = false;
        Boolean achouPreposicao = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < preposicao.length; s++) {
                if (tags[i].equals(preposicao[s]) && tokens[i].equals("that")) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPreposicao = true;
                    break;
                }
            }
            if (achouPreposicao == true) {
                break;
            }
        }

        for (int i = passaValor; i < tokens.length; i++) {
            for (int v = 0; v < verbo.length; v++) {
                if (tags[i].equals(verbo[v])) {
                    for (int in = 0; in < predictives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(predictives[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouPreposicao == true) {
            achouPredictive = true;
        }
        return achouPredictive;
    }

    /*
    Retrodictives: (recount, report)
In uttering e, S retrodicts that P if S expresses:
i. the belief that it was the case that P, and
ii. the intention that H believe that it was the case that P.
     */
    public static Boolean atoDeFalaRetrodictive(String mensagem, String[] retrodictives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] preposicao = {"IN", "TO"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouRetrodictive = false;
        Boolean achouPreposicao = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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

        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < preposicao.length; s++) {
                if (tags[i].equals(preposicao[s])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPreposicao = true;
                    break;
                }
            }
            if (achouPreposicao == true) {
                break;
            }
        }

        for (int i = passaValor; i < tokens.length; i++) {
            for (int v = 0; v < verbo.length; v++) {
                if (tags[i].equals(verbo[v])) {
                    for (int in = 0; in < retrodictives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(retrodictives[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouPreposicao == true) {
            achouRetrodictive = true;
        }
        return achouRetrodictive;
    }

    /*
    Descriptives: (appraise, assess, call, categorize, characterize, classify,
date, describe, diagnose, evaluate, grade, identify, portray, rank)
In uttering e, S describes 0 as F if S expresses:
i. the belief that 0 is F, and
ii. the intention that H believe that 0 is F.
     */
    public static Boolean atoDeFalaDescriptive(String mensagem, String[] descriptive) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] objeto = {"NN", "NNS", "NNP", "NNPS"};
        String[] preposicao = {"NN", "NNS", "NNP", "NNPS"};
        String[] adjetivo = {"JJ"};
        Boolean achouDescriptive = false;
        Boolean achouSujeito = false;
        Boolean achouObjeto = false;
        Boolean achouPreposicao = false;
        Boolean achouAdjetivo = false;
        Boolean achouVerbo = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
                    for (int in = 0; in < descriptive.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(descriptive[in]))) {
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
            for (int v = 0; v < objeto.length; v++) {
                if (tags[i].equals(objeto[v])) {
                    for (int in = 0; in < descriptive.length; in++) {
                        modelo = new Modelo();
                        modelo.setConteudo(tokens[i]);
                        modelo.setTag(tags[i]);
                        modelo.setPosicao(i);
                        respostas.add(modelo);
                        passaValor = i;
                        achouObjeto = true;
                        break;
                    }
                }
            }
            if (achouObjeto == true) {
                break;
            }
        }

        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < preposicao.length; o++) {
                if (tags[i].equals(preposicao[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPreposicao = true;
                    break;
                }
            }
            if (achouPreposicao == true) {
                break;
            }
        }

        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < adjetivo.length; o++) {
                if (tags[i].equals(adjetivo[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouAdjetivo = true;
                    break;
                }
            }
            if (achouAdjetivo == true) {
                break;
            }
        }

        if (achouSujeito == true && achouVerbo == true && achouPreposicao == true && achouObjeto == true && achouAdjetivo == true) {
            achouDescriptive = true;
        }
        return achouDescriptive;
    }

    /*
    Ascriptives: (ascribe, attribute, predicate)
In uttering e, S ascribes F to 0 if S expresses:
i. the belief that F applies to 0, and
ii. the intention that H believe that F applies to o.
     */
    public static Boolean atoDeFalaAscriptives(String mensagem, String[] ascriptives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        String[] preposicao = {"IN", "TO"};
        String[] pessoaObjeto = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouAscriptive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        Boolean achouPreposicao = false;
        Boolean achouPessoaObjeto = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
                    for (int in = 0; in < ascriptives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(ascriptives[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < preposicao.length; o++) {
                if (tags[i].equals(preposicao[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPreposicao = true;
                    break;
                }
            }
            if (achouPreposicao == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < pessoaObjeto.length; o++) {
                if (tags[i].equals(preposicao[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPessoaObjeto = true;
                    break;
                }
            }
            if (achouPessoaObjeto == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouPreposicao == true && achouPessoaObjeto == true) {
            achouAscriptive = true;
        }
        return achouAscriptive;
    }

    /*
    Confirmatives: (appraise, assess, bear witness, certify, conclude, confirm,
corroborate, diagnose, find, judge, substantiate, testify, validate,
verify, vouch for)
    In uttering e, S confirms (the claim) that P if S expresses:
i. the belief that P, based on some truth-seeking procedure, and
ii. the intention that H believe that P because S has support for P.
     */
    public static Boolean atoDeFalaConfirmative(String mensagem, String[] confirmatives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"PRP"};
        Boolean achouConfirmative = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
                    for (int in = 0; in < confirmatives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(confirmatives[in]))) {

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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    passaValor = i;
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true) {
            achouConfirmative = true;
        }
        return achouConfirmative;
    }

    /*
    Concessives: (acknowledge, admit, agree, allow, assent, concede, concur,
confess, grant, own)
In uttering e, S concedes that P if S expresses:
i. the belief that P, contrary to what he would like to believe or contrary
to what he previously believed or avowed, and
ii. the intention that H believe that P.
     */
    public static Boolean atoDeFalaConcessive(String mensagem, String[] concessives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        String[] concessao = {"VB", "VBD", "VBG", "VBZ"};
        Boolean achouConcessive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        Boolean achouConcessao = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
                    for (int in = 0; in < concessives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(concessives[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < concessao.length; o++) {
                if (tags[i].equals(concessao[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouConcessao = true;
                    break;
                }
            }
            if (achouConcessao == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouConcessao == true) {
            achouConcessive = true;
        }
        return achouConcessive;
    }

    /*
    Retractives: (abjure, correct, deny, disavow, disclaim, disown, recant,
renounce, repudiate, retract, take back, withdraw)
In uttering e, S retracts the claim that P if S expresses:
i. that he no longer believes that P, contrary to what he previously
indicated he believed, and
ii. the intention that H not believe that P.
     */
    public static Boolean atoDeFalaRetractive(String mensagem, String[] requestives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] objeto = {"NN", "NNS", "NNP", "NNPS", "PRP", "JJ"};
        String[] preposicao = {"NN", "NNS", "NNP", "NNPS", "PRP", "JJ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouRequestive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {

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
                    for (int in = 0; in < requestives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(requestives[in]))) {

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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {

                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true) {
            achouRequestive = true;
        }
        return achouRequestive;
    }

    /*
    Assentives: (accept, agree, assent, concur)
In uttering e, S assents to the claim that P if S expresses:
i. the belief that P, as claimed by H (or as otherwise under discussion),
and
ii. the intention (perhaps already fulfilled) that H believe that P.

     */
    public static Boolean atoDeFalaAssentive(String mensagem, String[] assentives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] preposicao = {"TO", "IN"};
        String[] objeto = {"NN", "NNS", "NNP", "NNPS"};
        Boolean achouAssentive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPreposicao = false;
        Boolean achouObjeto = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
                    for (int in = 0; in < assentives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(assentives[in]))) {
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
            for (int o = 0; o < preposicao.length; o++) {
                if (tags[i].equals(preposicao[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPreposicao = true;
                    passaValor = i;
                    break;
                }
            }
            if (achouPreposicao == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPreposicao == true && achouObjeto == true) {
            achouAssentive = true;
        }
        return achouAssentive;
    }

    /*
    Dissentives: (differ, disagree, dissent, reject)
In uttering e, S dissents from the claim that P if S expresses:
i. the disbelief that P, contrary to what was claimed by H (or was
otherwise under discussion), and
ii. the intention that H disbelieve that P.
     */
    public static Boolean atoDeFalaDissentive(String mensagem, String[] dissentives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] preposicao = {"IN"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouDissentive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPreposicao = false;
        Boolean achouPessoa = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < preposicao.length; s++) {
                if (tags[i].equals(preposicao[s])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPreposicao = true;
                    break;
                }
            }
            if (achouPreposicao == true) {
                break;
            }
        }

        for (int i = passaValor; i < tokens.length; i++) {
            for (int v = 0; v < verbo.length; v++) {
                if (tags[i].equals(verbo[v])) {
                    for (int in = 0; in < dissentives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(dissentives[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    passaValor = i;
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouPreposicao == true) {
            achouDissentive = true;
        }
        return achouDissentive;
    }

    /*
    Disputatives: (demur, dispute, object, protest, question)
In uttering e, S disputes the claim that P if S expresses:
i. the belief that there is reason not to believe that P, contrary to what
was claimed by H (or was otherwise under discussion), and
ii. the intention that H believe that there is reason not to believe that P.
     */
    public static Boolean atoDeFalaDisputative(String mensagem, String[] disputatives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] objeto = {"NN", "NNS", "NNP", "NNPS"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouDisputative = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        Boolean achouObjeto = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < objeto.length; s++) {
                if (tags[i].equals(objeto[s])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouObjeto = true;
                    break;
                }
            }
            if (achouObjeto == true) {
                break;
            }
        }

        for (int i = passaValor; i < tokens.length; i++) {
            for (int v = 0; v < verbo.length; v++) {
                if (tags[i].equals(verbo[v])) {
                    for (int in = 0; in < disputatives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(disputatives[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    passaValor = i;
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouObjeto == true) {
            achouDisputative = true;
        }
        return achouDisputative;
    }

    /*
    Responsives: (answer, reply, respond, retort)
In uttering e, S responds that P if S expresses:
i. the belief that P, which H has inquired about, and
ii. the intention that H believe that P.
     */
    public static Boolean atoDeFalaResponsives(String mensagem, String[] responsives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouResponsive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {

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
                    for (int in = 0; in < responsives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(responsives[in]))) {

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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {

                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true) {
            achouResponsive = true;
        }
        return achouResponsive;
    }

    /*
    Suggestives: (conjecture, guess, hypothesize, speculate, suggest)
In uttering e, S suggests that P if S expresses
    i. the belief that there is reason, but not sufficient reason, to believe
that P, and
ii. the intention that H believe that there is reason, but not sufficient
reason, to believe that P.
     */
    public static Boolean atoDeFalaSuggestives(String mensagem, String[] suggestives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        String[] sugestao = {"VB", "VBD", "VBG", "VBZ"};
        Boolean achouSuggestive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        Boolean achouSugestao = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
                    for (int in = 0; in < suggestives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(suggestives[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                passaValor = i;
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPessoa = true;
                    passaValor = i;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < sugestao.length; o++) {
                if (tags[i].equals(sugestao[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    passaValor = i;
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouSugestao == true) {
            achouSuggestive = true;
        }
        return achouSuggestive;
    }

    /*
    Suppositives: (assume, hypothesize, postulate, stipulate, suppose, theorize)
In uttering e, S supposes that P if S expresses:
i. the belief that it is worth considering the consequences of P, and
ii. the intention that H believe that it is worth considering the consequences
of P.
     */
    public static Boolean atoDeFalaSuppositive(String mensagem, String[] suppositives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouSuppositive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {

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
                    for (int in = 0; in < suppositives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(suppositives[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {

                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true) {
            achouSuppositive = true;
        }
        return achouSuppositive;
    }

    /*
    Questions: (ask, inquire, interrogate, query, question, quiz)
In uttering e, S questions H as to whether or not P if S expresses:
i. the desire that H tell S whether or not P, and
ii. the intention that H tell S whether or not P because of S's desire.
     */
    public static Boolean atoDeFalaQuestions(String mensagem, String[] questions) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouQuestion = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        Boolean achouInterrogacao = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {

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
                    for (int in = 0; in < questions.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(questions[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    passaValor = i;
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < pessoa.length; o++) {
                if (tokens[i].equals("?")) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    passaValor = i;
                    respostas.add(modelo);
                    achouInterrogacao = true;
                    break;
                }
            }
            if (achouInterrogacao == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouInterrogacao== true) {
            achouQuestion = true;
        }
        return achouQuestion;
    }

    /*
    Requirements: (bid, charge, command, demand, dictate, direct, enjoin,
instruct, order, prescribe, require)
In uttering e, S requires H to A if S expresses:
i. the belief that his utterance, in virtue of his authority over H, constitutes
sufficient reason for H to A, and
ii. the intention that H do A because of S's utterance.
     */
    public static Boolean atoDeFalaRequirements(String mensagem, String[] requirements) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        String[] objeto = {"NN", "NNS", "NNP", "NNPS"};
        String[] preposicao = {"TO"};
        Boolean achouRequerimento = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        Boolean achouObjeto = false;
        Boolean achouPreposicao = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
                    for (int in = 0; in < requirements.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(requirements[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
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
                    passaValor = i;
                    achouObjeto = true;
                    break;
                }
            }
            if (achouObjeto == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < preposicao.length; o++) {
                if (tags[i].equals(preposicao[o]) && tokens[i].toLowerCase().equals("to")) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPreposicao = true;
                    break;
                }
            }
            if (achouPreposicao == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouObjeto == true && achouPreposicao == true) {
            achouRequerimento = true;
        }
        return achouRequerimento;
    }

    /*
    Prohibitives: (enjoin, forbid, prohibit, proscribe, restrict)
In uttering e, S prohibits H from A-ing if S expresses:
i. the belief that his utterance, in virtue of his authority over H, constitutes
sufficient reason for H not to A, and
ii. the intention that because of S's utterance H not do A.
     */
    public static Boolean atoDeFalaProhibitive(String mensagem, String[] proibitives) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        String[] proibicao = {"VB", "VBD", "VBG"};
        String[] preposicao = {"IN"};
        Boolean achouProibitive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        Boolean achouProibicao = false;
        Boolean achouPreposicao = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
                    for (int in = 0; in < proibitives.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(proibitives[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    passaValor = i;
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < proibicao.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouProibicao = true;
                    break;
                }
            }
            if (achouProibicao == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < preposicao.length; o++) {
                if (tags[i].equals(preposicao[o]) && tokens[i].equals("from")) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPreposicao = true;
                    break;
                }
            }
            if (achouPreposicao == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouProibicao == true && achouPreposicao == true) {
            achouProibicao = true;
        }
        return achouProibicao;
    }

    /*
    Permissives: (agree to, allow, authorize, bless, consent to, dismiss,
excuse, exempt, forgive, grant, license, pardon, release, sanction)
In uttering e, S permits H to A if S expresses:
i. the belief that his utterance, in virtue of his authority over H, entitles
H to A, and
ii. the intention that H believe that S's utterance entitles him to A.
     */
    public static Boolean atoDeFalaPermissive(String mensagem, String[] permissive) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        String[] permissao = {"VB", "VBD", "VBG"};
        Boolean achouRequestive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        Boolean achouPermissao = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
                    for (int in = 0; in < permissive.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(permissive[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    passaValor = i;
                    respostas.add(modelo);
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < permissao.length; o++) {
                if (tags[i].equals(permissao[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouPermissao == true) {
            achouRequestive = true;
        }
        return achouRequestive;
    }

    /*
    Advisories: (admonish, advise, caution, counsel, propose, recommend,
suggest, urge, warn)
In uttering e, S advises H to A if S expresses:
i. the belief that there is (sufficient) reason for H to A, and
ii. the intention thatH take S's belief as (sufficient) reason for him toA.
     */
    public static Boolean atoDeFalaAdvisories(String mensagem, String[] advisories) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        String[] conselho = {"VB", "VBD", "VBG"};
        Boolean achouRequestive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        Boolean achouConselho = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {

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
                    for (int in = 0; in < advisories.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(advisories[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {

                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouPessoa = true;
                    passaValor = i;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < conselho.length; o++) {
                if (tags[i].equals(conselho[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouConselho == true) {
            achouRequestive = true;
        }
        return achouRequestive;
    }

    /*
    Promises: (promise, swear, vow)
In uttering e, S promises H to A if S expresses:
i. the belief that his utterance obligates him to A,
ii. the intention to A, and
iii. the intention that H believe that S's utterance obligates S to A and
that S intends to A.
     */
    public static Boolean atoDeFalaPromisse(String mensagem, String[] promisses) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouRequestive = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achoupessoa = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {
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
                    for (int in = 0; in < promisses.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(promisses[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {

                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achoupessoa = true;
                    break;
                }
            }
            if (achoupessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achoupessoa == true) {
            achouRequestive = true;
        }
        return achouRequestive;
    }
//        Offers: (offer , propose)
//In uttering e, S offers A to H if S expresses:
//i. the belief that S's utterance obligates him to A on condition that H
//indicates he wants S to A,
//ii. the intention to A on condition that H indicates he wants S to A, and
//iii. the intention that H believe that S's utterance obligates S to A and
//that S intends to A, on condition that H indicates he wants S to A.
//volunteer: S offers his services.
//bid: S offers to give something (in a certain amount) in exchange for
//something.

    public static Boolean atoDeFalaOffer(String mensagem, String[] offers) {
        POSModel model = new POSModelLoader().load(new File("C:\\SpeechActMiner\\lib\\en-pos-maxent.bin"));
        POSTaggerME tagger = new POSTaggerME(model);
        String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(mensagem);
        String[] tags = tagger.tag(tokens);
        String[] sujeito = {"PRP"};
        String[] verbo = {"VB", "VBD", "VBG", "VBZ"};
        String[] pessoa = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        String[] pronome = {"NN", "NNS", "NNP", "NNPS", "PRP"};
        Boolean achouOffer = false;
        Boolean achouSujeito = false;
        Boolean achouVerbo = false;
        Boolean achouPessoa = false;
        Boolean achouObjetoPronome = false;
        List<Modelo> respostas = new ArrayList<>();
        Modelo modelo = null;
        int passaValor = 0;
        for (int i = 0; i < tokens.length; i++) {
            for (int s = 0; s < sujeito.length; s++) {
                if (tags[i].equals(sujeito[s])) {

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
                    for (int in = 0; in < offers.length; in++) {
                        if (convertendoParaInfinitivo(tokens[i]).equals(convertendoParaInfinitivo(offers[in]))) {
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
            for (int o = 0; o < pessoa.length; o++) {
                if (tags[i].equals(pessoa[o])) {

                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    passaValor = i;
                    achouPessoa = true;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        for (int i = passaValor; i < tokens.length; i++) {
            for (int o = 0; o < pronome.length; o++) {
                if (tags[i].equals(pronome[o])) {
                    modelo = new Modelo();
                    modelo.setConteudo(tokens[i]);
                    modelo.setTag(tags[i]);
                    modelo.setPosicao(i);
                    respostas.add(modelo);
                    achouObjetoPronome = true;
                    passaValor = i;
                    break;
                }
            }
            if (achouPessoa == true) {
                break;
            }
        }
        if (achouSujeito == true && achouVerbo == true && achouPessoa == true && achouObjetoPronome == true) {
            achouOffer = true;
        }
        return achouOffer;
    }
}
