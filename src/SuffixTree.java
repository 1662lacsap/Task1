/*
Zadanie 1
Majac dany zbior Z złozony z k lancuchow, chcemy sprawdzic ile z tych lancuchow jest podlanuchem jakiegos
innego lanucha ze zbioru Z.

Zakladajac ze suma dlugosci wszystkich lanuchow wynosi m, zaprojektuj i zaimplementuj algorytm rozwiazaujacy
to zadanie w czasie O(m0
*/

//Definicje:
//s - tekst, ciąg symboli s = s1,s2,...sn nalezacych do alfabetu
//n - Dlugosc tekstu (liczba jego elementów)
//p -pattern (wzorzec)

import java.util.*;


public class SuffixTree {

    static String ALPHABET = "";
    static CharSequence s = "";

    public static class Node {

        int begin;
        int end;
        int depth; //distance in characters from root to this node
        Node parent;
        Node suffixLink;

        Map<Character, Node> children;  //zamiast Node[] children
        int numberOfLeaves;             //zliczamy liscie

        Node(int begin, int end, int depth, int noleaf, Node parent) {

            this.begin = begin;
            this.end = end;
            this.depth = depth;
            this.parent = parent;
            children = new HashMap<>();
            numberOfLeaves = noleaf;

        }
    }

    private static Node buildSuffixTree(CharSequence s) {

        //return_s(s.toString());
        SuffixTree.s = s;

        int n = s.length();
        byte[] a = new byte[n];

        for (int i = 0; i < n; i++) {
            a[i] = (byte) ALPHABET.indexOf(s.charAt(i));
        }

        Node root = new Node(0, 0, 0, 0, null);
        Node node = root;

        for (int i = 0, tail = 0; i < n; i++, tail++) {

            //ustaw ostatni stworzony węzeł wewnętrzny na null przed rozpoczęciem każdej fazy.
            Node last = null;

            //tail - tyle sufiksów musi zostać utworzone.
            while (tail >= 0) {
                Node ch = node.children.get(ALPHABET.charAt(a[i - tail]));
                while (ch != null && tail >= ch.end - ch.begin) {

                    //liscie
                    node.numberOfLeaves++;

                    tail -= ch.end - ch.begin;
                    node = ch;
                    ch = ch.children.get(ALPHABET.charAt(a[i - tail]));
                }

                if (ch == null) {
                    // utworz nowy Node z bieżącym znakiem
                    node.children.put(ALPHABET.charAt(a[i]),
                            new Node(i, n, node.depth + node.end - node.begin, 1, node));

                    // liscie
                    node.numberOfLeaves++;

                    if (last != null) {
                        last.suffixLink = node;
                    }
                    last = null;
                } else {
                    byte t = a[ch.begin + tail];
                    if (t == a[i]) {
                        if (last != null) {
                            last.suffixLink = node;
                        }
                        break;
                    } else {
                        Node splitNode = new Node(ch.begin, ch.begin + tail,
                                node.depth + node.end - node.begin, 0, node);
                        splitNode.children.put(ALPHABET.charAt(a[i]),
                                new Node(i, n, ch.depth + tail, 1, splitNode));

                        //liscie
                        splitNode.numberOfLeaves++;

                        splitNode.children.put(ALPHABET.charAt(t), ch);

                        //liscie
                        splitNode.numberOfLeaves += ch.numberOfLeaves;

                        ch.begin += tail;
                        ch.depth += tail;
                        ch.parent = splitNode;
                        node.children.put(ALPHABET.charAt(a[i - tail]), splitNode);

                        //liscie
                        node.numberOfLeaves++;

                        if (last != null) {
                            last.suffixLink = splitNode;
                        }
                        last = splitNode;
                    }
                }
                if (node == root) {
                    --tail;
                } else {
                    node = node.suffixLink;
                }
            }
        }
        return root;
    }

    private static void print(CharSequence s, int i, int j) {
        for (int k = i; k < j; k++) {
            System.out.print(s.charAt(k));
        }
    }

    // Jesli chcemy wydrukowac drzewo nalezy odkomentowac w main
    private static void printTree(Node n, CharSequence s, int spaces) {
        int i;
        for (i = 0; i < spaces; i++) {
            System.out.print("␣");
        }
        print(s, n.begin, n.end);
        System.out.println("␣" + (n.depth + n.end - n.begin));

        for (Node child : n.children.values()) {
            if (child != null) {
                printTree(child, s, spaces + 4);
            }
        }

    }

    /*##########################################################################################*/
    //Budujemy drzewo sufiksowe dla calego zbioru lancuchow zamiast jedneho lancucha,
    //tzw. uogolnione drzewo sufiksowe. Lączymy wszystkie lancuchy w jeden lancuch za pomoca wybranego znaku separatora
    //i konstrujemy drzewo sufiksowe dla takiego dlugiego lancucha - liscie zliczamy wczesniej w czasie tworzenia
    //tego drzewa.

    // w uogolnionym drzewie sufiksowym
    // szukamy gdzie ilosc lisci dla wyszukiwanego wzorca jest wieksza od 1
    private static boolean isContiguousSequenceCharactersString(Node root, CharSequence p) {

        Node actualNode = root;
        int index_p = 0;
        int index_s;
        int length_p = p.length();

        while (index_p < length_p) {
            actualNode = actualNode.children.get(p.charAt(index_p));

            if (actualNode == null) {
                return false;
            }

            index_s = actualNode.begin;


            do {

                if (p.charAt(index_p++) != s.charAt(index_s++))

                    return false;

            } while
            (index_s < actualNode.end && index_p < length_p);

        }

        return actualNode.numberOfLeaves > 1;
    }


    //Suma True z wczesniejszej funkcji - liczymy liczbę podlancuchow wiekszych niz 1.
    private static int numberOfSubstrings(Node root, Set<CharSequence> chains) {

        int numberOfSubstrings = 0;

        for (CharSequence p : chains) {
            if (isContiguousSequenceCharactersString(root, p))
                numberOfSubstrings++;
        }

        return numberOfSubstrings;
    }

    /*##########################################################################################*/

    // funkcja pomocnicza ustawiająca ALPHABET
    private static void saveAlphabet(String s) {
        final Set<Character> set = new HashSet<>();
        for (int i = 0; i < s.length(); i++) {
            set.add(s.charAt(i));
        }

        StringBuilder alphabetS = new StringBuilder();
        for (char ch : set) {
            alphabetS.append(ch);
        }
        ALPHABET = alphabetS.toString();
    }


    // main - Test
    public static void main(String[] args) {

        try {

            Set<CharSequence> chains = new HashSet<>();

            //Test
            //Zbior Z przykladowe lancuchy, w tym przykladzie tylko "szkola" i "samochod"
            //jest podlancuchem w innym lancuchu - zatem wynik 2
            chains.add("uczelniaszkola");
            chains.add("szkola");
            chains.add("szkolasamochodszkola");
            chains.add("samochod");
            chains.add("babcia");
            chains.add("kanarek");
            chains.add("zegarek");
            chains.add("muzeum");
            chains.add("punto");
            chains.add("miasto");

            String k_chain = "";
            for (CharSequence chain : chains) {
                k_chain += chain + "$";
            }

            //wyswietla zląnczony lancuch za pomoca wybranego znaku separatora
            System.out.println(" ");
            System.out.println("Zlączony jeden dlugi lancuch za pomoca wybranego znaku separatora z badanego zbioru Z.\n" +
                    "W tym przykladzie tylko \"szkola\" i \"samochod\"" +
                    "jest podlancuchem w innym lancuchu - zatem wynik 2."+"\nOto jeden dlugi lancuch:" );
            System.out.println(k_chain);
            System.out.println(" ");

            // String s z k lancuchow
            String s = k_chain;

            //pomocniczo ustawiamy ALPHABET
            saveAlphabet(s);

            //Uogolnione drzewo sufiksowe
            Node root = buildSuffixTree(s);

            //Jesli chcemy wydrukowac drzewo nalezy odkomentowac
           // printTree(root, s, 0);

            System.out.println("Wynik: ");
            System.out.println(numberOfSubstrings(root, chains) + " lancuch(y) są podlanuchami jakiegos innego lancucha " +
                    "z badanego zbioru Z");
            //koniec Test-u


            // Skaner do testow umowliwia wprowadzanie wielu lancuchow az do nacisniecia spacji i enter
            Set<CharSequence> chains2 = new HashSet<>();

            //Test
            System.out.println(" ");
            System.out.println("Wykonaj wiecej testow: ");
            System.out.println("Podaj lancuchy do zbioru Z po kazdym naciskajac enter, jesli zamiast lancucha " +
                    "zostanie wybrana spacja i enter zakonczy sie wprowadzanie lancuchow do zbioru Z: ");

            Scanner tekst_s = new Scanner(System.in); //obiekt do odebrania danych od użytkownika

        while (!(s.equals(" "))) {
                s = tekst_s.nextLine();
                chains2.add(s);
            }


            String k_chain2 = "";
            for (CharSequence chain : chains2) {
                k_chain2 += chain + "$";
            }

            //wyswietla zląnczony lancuch za pomoca wybranego znaku separatora
            System.out.println("Zlączony jeden dlugi lancuch za pomoca wybranego znaku separatora $ " +
                    "z badanego zbioru Z:");

            String s1 = k_chain2.replace(" $","");
            System.out.println(s1);
            System.out.println(" ");

            //pomocniczo ustawiamy ALPHABET
            saveAlphabet(s1);

            // Uogolnione drzewo sufiksowe
            Node root2 = buildSuffixTree(s1);

            //Jesli chcemy wydrukowac drzewo nalezy odkomentowac
            // printTree(root2, s1, 0);

            System.out.println("Wynik: ");
            System.out.println(numberOfSubstrings(root2, chains2) + " lancuch(y) są podlanuchami jakiegos " +
                    "innego lancucha z badanego zbioru Z");


        }
        catch(StringIndexOutOfBoundsException err){
            System.out.println("USTAW ALPHABET "+err);
        }

    }

}




