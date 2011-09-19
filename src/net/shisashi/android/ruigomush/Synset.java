/**
 * 同義語bean
 * @author S.Hisashi
 */
package net.shisashi.android.ruigomush;

public class Synset {
    public final int id;
    public final String definition;
    public final String[] words;

    public Synset(int id, String definition, String[] words) {
        this.id = id;
        this.definition = definition;
        this.words = words;
    }

    public Synset(int id, String[] splited) {
        this.id = id;
        this.definition = splited[0];
        this.words = new String[splited.length-1];
        for (int i = 1; i < splited.length; i++) {
            words[i-1] = splited[i];
        }
    }
}
