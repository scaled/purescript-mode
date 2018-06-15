//
// Scaled PureScript Mode - a Scaled major mode for editing PureScript code
// http://github.com/scaled/purescript-mode/blob/master/LICENSE

package scaled.project

import com.eclipsesource.json._
import java.nio.file.{Files, Path}
import scaled._

object PureScriptPlugins {

  val PscPkgFile = "psc-package.json"
  val ProjectFiles = Seq("bower.json", PscPkgFile)
  val NodeModules = "node_modules"
  val PLSModule = "purescript-language-server"

  @Plugin(tag="project-root")
  class PscPkgRootPlugin extends RootPlugin.File(PscPkgFile)

  @Plugin(tag="project-resolver")
  class PscPkgResolverPlugin extends ResolverPlugin {

    override def metaFiles (root :Project.Root) = Seq(root.path.resolve(PscPkgFile))

    def addComponents (project :Project) {
      val rootPath = project.root.path
      val bowerFile = rootPath.resolve(PscPkgFile)
      val config = Json.parse(Files.newBufferedReader(bowerFile)).asObject

      val projName = Option(config.get("name")).map(_.asString).
        getOrElse(rootPath.getFileName.toString)

      val sb = Ignorer.stockIgnores
      Option(config.get("ignore")).map(_.asArray).foreach { ignores =>
        // TODO: handle glob ignores properly
        ignores.map(_.asString).foreach { sb += Ignorer.ignoreName(_) }
      }
      project.addComponent(classOf[Filer], new DirectoryFiler(project, sb))

      // TODO: bower doesn't define source directories, so we hack some stuff
      val sourceDirs = Seq("src", "test").map(rootPath.resolve(_))
      project.addComponent(classOf[Sources], new Sources(sourceDirs))

      val oldMeta = project.metaV()
      project.metaV() = oldMeta.copy(name = projName)
    }
  }

  @Plugin(tag="langserver")
  class PureScriptLangPlugin extends LangPlugin {
    def suffs (root :Project.Root) = Set("purs")
    def canActivate (root :Project.Root) =
      ProjectFiles.exists(p => Files.exists(root.path.resolve(p))) &&
      Files.exists(root.path.resolve(NodeModules).resolve(PLSModule))
    def createClient (proj :Project) = Future.success(
      new PureScriptLangClient(proj.metaSvc, proj.root.path, serverCmd(proj.root.path)))
  }

  private def serverCmd (root :Path) :Seq[String] = {
    val havePscPkg = Files.exists(root.resolve(PscPkgFile))
    val config = s"""{"purescript": {"addPscPackageSources": $havePscPkg}}"""
    // TODO: other config?

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
