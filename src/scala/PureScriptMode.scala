//
// Scaled PureScript Mode - a Scaled major mode for editing PureScript code
// http://github.com/scaled/purescript-mode/blob/master/LICENSE

package scaled.purescript

import scaled._
import scaled.code.Indenter
import scaled.grammar._
import scaled.code.{CodeConfig, Commenter}

@Plugin(tag="textmate-grammar")
class PureScriptGrammarPlugin extends GrammarPlugin {
  import EditorConfig._
  import CodeConfig._

  override def grammars = Map("source.purescript" -> "PureScript.ndf")

  override def effacers = List(
    effacer("comment.line.documentation", docStyle),
    effacer("comment.block.documentation", docStyle),
    effacer("comment.line", commentStyle),
    effacer("comment.block", commentStyle),
    effacer("constant", constantStyle),
    effacer("invalid", warnStyle),
    effacer("keyword.directive", moduleStyle),
    effacer("keyword", keywordStyle),
    effacer("string", stringStyle),
    effacer("variable", variableStyle),
    effacer("entity.name.function", functionStyle),
    effacer("entity.name.type.module", moduleStyle),
    effacer("entity.name.type", typeStyle),
    effacer("support.other.module", moduleStyle),
    effacer("storage.type.class", keywordStyle),
    effacer("storage", variableStyle)
  )

  override def syntaxers = List(
    syntaxer("comment.line.documentation", Syntax.DocComment),
    syntaxer("comment.block.documentation", Syntax.DocComment),
    syntaxer("comment.line", Syntax.LineComment),
    syntaxer("comment.block", Syntax.BlockComment),
    syntaxer("constant", Syntax.OtherLiteral),
    syntaxer("string.quoted.triple", Syntax.HereDocLiteral),
    syntaxer("string", Syntax.StringLiteral)
  )
}

@Major(name="purescript",
       tags=Array("code", "project", "purescript"),
       pats=Array(".*\\.purs"),
       desc="A major mode for editing PureScript code.")
class PureScriptMode (env :Env) extends GrammarCodeMode(env) {

  override def dispose () :Unit = {} // nada for now

  override def langScope = "source.purescript"

  override def keymap = super.keymap.
    bind("self-insert-command", "'"); // don't auto-pair single quote

  // HACK: leave indent as-is
  override def computeIndent (row :Int) :Int = Indenter.readIndent(buffer, Loc(row, 0))

  override val commenter = new Commenter {
    override def linePrefix  = "--"
    override def blockOpen = ""
    override def blockClose = ""
    override def blockPrefix = ""
    override def docOpen   = "-- |"
    override def docPrefix   = "-- |"
  }
}
