package io.github.semyonsinchenko.sparkss.expressions.support

object UnicodeFixtures {

  final case class UnicodePair(name: String, left: String, right: String)

  val LatinDiacritics: Seq[UnicodePair] = Seq(
    UnicodePair("ascii-baseline", "cafe", "cafe"),
    UnicodePair("precomposed-vs-ascii", "caf\u00E9", "cafe"),
    UnicodePair("decomposed-vs-precomposed", "caf\u0065\u0301", "caf\u00E9"),
    UnicodePair("accented-word", "r\u00E9sum\u00E9", "resume"),
    UnicodePair("umlaut-vs-digraph", "M\u00FCller", "Mueller")
  )

  val CjkPairs: Seq[UnicodePair] = Seq(
    UnicodePair("cjk-identical", "\u6771\u4EAC", "\u6771\u4EAC"),
    UnicodePair("cjk-different", "\u6771\u4EAC", "\u5927\u962A"),
    UnicodePair("cjk-partial", "\u6771\u4EAC \u90FD", "\u6771\u4EAC")
  )

  val EmojiPairs: Seq[UnicodePair] = Seq(
    UnicodePair("emoji-identical", "\uD83D\uDE00", "\uD83D\uDE00"),
    UnicodePair("emoji-different", "\uD83D\uDE00", "\uD83D\uDE01")
  )

  val MixedScriptPairs: Seq[UnicodePair] = Seq(
    UnicodePair("latin-cjk-mixed", "hello \u4E16\u754C", "hello world"),
    UnicodePair("latin-cyrillic-mixed", "John \u0421\u043C\u0438\u0442", "John Smith")
  )

  val ZeroWidthPairs: Seq[UnicodePair] = Seq(
    UnicodePair("zero-width-space", "alpha\u200Bbeta", "alpha beta"),
    UnicodePair("word-joiner", "alpha\u2060beta", "alpha beta")
  )

  val NoWhitespaceCjkPairs: Seq[UnicodePair] = Seq(
    UnicodePair("cjk-no-whitespace-identical", "\u5317\u4EAC\u5927\u5B66", "\u5317\u4EAC\u5927\u5B66"),
    UnicodePair("cjk-no-whitespace-different", "\u5317\u4EAC\u5927\u5B66", "\u5317\u4EAC\u5927\u5B66\u9662")
  )

  val AllPairs: Seq[UnicodePair] =
    LatinDiacritics ++ CjkPairs ++ EmojiPairs ++ MixedScriptPairs ++ ZeroWidthPairs ++ NoWhitespaceCjkPairs
}
