#! /usr/bin/env python
# -*- coding: utf-8 -*-

from xml.etree.cElementTree import ElementTree
import codecs
import sys
import re

#CODING = 'cp932'
#ERRORS = 'backslashreplace' # 'xmlcharrefreplace'
#FILENAME_SUFFIX = 'sj'

CODING = 'utf-8'
ERRORS = 'strict'
FILENAME_SUFFIX = 'u8'

class Synset(object):
    def __init__(self, ssid, definition):
        self.ssid = ssid
        self.definition = definition
        self.words = []
        # "(数値)年）" を含むものは固有名詞
        self.is_proper_noun = definition is not None and re.search(u'\\d年）', definition) is not None

    def __repr__(self):
        s = u'%s(%s)' % (self.ssid, u','.join(self.words))
        return s.encode('utf-8')

def generate_graph(tree):
    lexicon = tree.find('Lexicon')

    # synsetの一覧を読み込む
    synsets = {}
    ignored = []
    for ss in lexicon.getiterator('Synset'):
        ssid = ss.attrib['id'][8:] # 'jpn-1.1-' を除く
        de = ss.find('Definition')
        if de is None:
            # 定義文がない
            definition = None
        else:
            definition = de.attrib['gloss']

        synset = Synset(ssid, definition)
        if synset.is_proper_noun:
            # 固有名詞の類なので除外する
            ignored.append(synset)
        else:
            synsets[ssid] = synset

    # 単語の一覧を読み込み、定義にぶら下げる
    # 単語の出現数を数える
    word_counter = {}
    for le in lexicon.getiterator('LexicalEntry'):
        lemma = le.find('Lemma').attrib['writtenForm']
        n = 0
        for ss in le.getiterator('Sense'):
            ssid = ss.attrib['synset'][8:] # 'jpn-1.1-' を除く
            synset = synsets.get(ssid)
            if synset is not None:
                synset.words.append(lemma)
                n += 1
        word_counter[lemma] = word_counter.get(lemma, 0) + n

    # 不要なsynsetを取り除く
    # 必要なものを 単語 -> [synset] に変換
    del_ssids = []
    word_table = {}
    for ssid in synsets:
        synset = synsets[ssid]
        num_words = len(synset.words)
        if num_words == 0:
            # 単語が含まれない
            pass
        elif synset.definition is None and num_words == 1:
            # 定義文がなく、ぶら下がっている単語が1つ
            pass
        elif max(word_counter[word] for word in synset.words) == 1:
            # 含まれる全ての単語がこの意味しか持っていない
            pass
        else:
            # 削除対象外なので、単語 -> [synset] に追加
            for word in synset.words:
                word_table.setdefault(word, []).append(synset)
            continue
        # 上記の条件に該当したものを削除する
        del_ssids.append(ssid)

    # 削除対象を削除
    for ssid in del_ssids:
        del synsets[ssid]

    # 1つの単語が複数のsynsetを持っているが、どのsynsetも1つの単語(この単語)しか持っていない
    for word in word_table:
        word_synsets = word_table[word]
        if max(len(synset.words) for synset in word_synsets) == 1:
            for synset in word_synsets:
                del synsets[synset.ssid]

    # リストに変換して戻す
    return ((synsets[ssid] for ssid in synsets), ignored)

def write_ss(synset_list):
    with codecs.open('ss.' + FILENAME_SUFFIX + '.csv', 'w', encoding=CODING, errors=ERRORS) as sscsv:
        for synset in synset_list:
            #sscsv.write(synset.ssid)
            #sscsv.write(u'\t')
            definition = synset.definition
            if definition is None:
                definition = u''
            sscsv.write(definition)
            sscsv.write(u'\t')
            sscsv.write(u'\t'.join(synset.words))
            sscsv.write(u'\n')

def write_ig(ignored_list):
    with codecs.open('ig.' + FILENAME_SUFFIX + '.csv', 'w', encoding=CODING, errors=ERRORS) as igcsv:
        for ig in ignored_list:
            igcsv.write(ig.ssid)
            igcsv.write(u'\t')
            igcsv.write(ig.definition)
            igcsv.write(u'\n')

def main(filename):
    import time

    def ptime(message):
        print time.strftime('%Y-%m-%d %H:%M:%S'), message

    ptime('parsing a xml...')
    tree = ElementTree()
    tree.parse(filename)
    ptime('parsed.')

    ptime('generating a graph...')
    synset_list, ignored_list = generate_graph(tree)
    ptime('generated.')

    ptime('writeing ss.csv...')
    write_ss(synset_list)
    ptime('wrote.')

    ptime('writeing ig.csv...')
    write_ig(ignored_list)
    ptime('wrote.')

if __name__ == '__main__':
    #import psyco
    #psyco.full()
    main('jpn_wn_lmf.xml')
