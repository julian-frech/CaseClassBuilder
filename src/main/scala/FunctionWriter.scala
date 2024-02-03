import scala.io.Source
import java.io.{File, PrintWriter}
import java.awt.Toolkit
import java.awt.datatransfer.{StringSelection, Clipboard}

object FunctionWriter {

  def mergeOrUpdateFunction(filename: String, newFunction: String, functionName: String): Unit = {
    val file = new File(filename)
    val fileContent = if (file.exists()) Source.fromFile(file).getLines.mkString("\n") else ""

    val dataMocksPattern = "(?s)(object\\s+DataMocks\\s*\\{)(.*?)(\\})".r
    val functionPattern = s"(?s)(def\\s+$functionName\\s*\\(.*?\\)\\s*:\\s*\\w+\\s*=\\s*\\{)(.*?)(\\})".r

    val newFileContent = fileContent match {
      case dataMocksPattern(start, content, end) =>
        val newContent = functionPattern.findFirstMatchIn(content) match {
          case Some(_) => functionPattern.replaceAllIn(content, s"$$1$newFunction$$3")
          case None => content + "\n\n  " + newFunction
        }
        s"$start$newContent$end"

      case _ => s"object DataMocks {\n\n  $newFunction\n\n}"
    }

    val pw = new PrintWriter(file)
    try {
      pw.write(newFileContent)
    } finally {
      pw.close()
    }
  }

  def copyToClipboard(text: String): Unit = {
    val stringSelection = new StringSelection(text)
    val clipboard: Clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
    clipboard.setContents(stringSelection, null)
    println("Text copied to clipboard.")
  }

}