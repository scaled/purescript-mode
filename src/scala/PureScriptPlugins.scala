//
// Scaled PureScript Mode - a Scaled major mode for editing PureScript code
// http://github.com/scaled/purescript-mode/blob/master/LICENSE

package scaled.project

import java.nio.file.{Files, Path}
import scaled._

object PureScriptPlugins {

  val BowerFile = "bower.json"
  val NodeModules = "node_modules"
  val PLSModule = "purescript-language-server"

  @Plugin(tag="langserver")
  class PureScriptLangPlugin extends LangPlugin {
    def suffs (root :Project.Root) = Set("purs")
    def canActivate (root :Project.Root) = Files.exists(root.path.resolve(BowerFile)) &&
      Files.exists(root.path.resolve(NodeModules).resolve(PLSModule))
    def createClient (metaSvc :MetaService, root :Project.Root) =
      Future.success(new PureScriptLangClient(metaSvc, root.path, serverCmd(root.path)))
  }

  private def serverCmd (root :Path) :Seq[String] = {
    // TODO: other config
    val config = """{"autoStartPscIde": true}"""

    Seq("node",
        root.resolve(NodeModules).resolve(PLSModule).resolve("cli.js").toString,
        "--stdio",
        "--config",
        config)
  }
}

class PureScriptLangClient (msvc :MetaService, root :Path, serverCmd :Seq[String])
    extends LangClient(msvc, root, serverCmd) {

  override def name = "PSC-IDE"
}
