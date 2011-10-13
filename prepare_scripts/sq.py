#! /usr/bin/env python
# -*- coding: utf-8 -*-
import sqlite3
import codecs

def init(con):
    con.execute('DROP TABLE IF EXISTS synset')
    con.execute('DROP TABLE IF EXISTS wordset')
    con.execute("CREATE TABLE android_metadata (locale text default 'en_US'); ")
    con.execute('''CREATE TABLE synset (id INTEGER PRIMARY KEY, definition TEXT, words TEXT)''')
    con.execute('''CREATE TABLE wordset (sid INTEGER, word text,
    PRIMARY KEY(sid, word),
    FOREIGN KEY(sid) REFERENCES synset(id)
    )''')
    con.execute('''CREATE INDEX wordset_word ON wordset(word)''')
    return con

def read_synsets(filename):
    with codecs.open(filename, 'r', 'utf-8') as f:
        synsets = []
        for lno, line in enumerate(f):
            line = line.strip()
            sp = line.split('\t')

            definition = sp[0]
            words = sp[1:]

            synsets.append((lno, definition, words))
        return synsets

def write_to_sqlite(con, synsets):
    synset_data = []
    wordset_data = []
    for synset in synsets:
        id_ = synset[0]
        definition = synset[1]
        words = synset[2]
        words.sort()

        synset_data.append((id_, definition, u'\t'.join(words)))
        for word in words:
            wordset_data.append((id_, word))

    con.executemany(u'INSERT INTO synset VALUES (?, ?, ?)', synset_data)
    con.executemany(u'INSERT INTO wordset VALUES (?, ?)', wordset_data)

    con.commit()
    con.close()

if __name__ == '__main__':
    con = sqlite3.connect('test1.db')
    synsets = read_synsets('ss.u8.csv')
    init(con)
    write_to_sqlite(con, synsets)
