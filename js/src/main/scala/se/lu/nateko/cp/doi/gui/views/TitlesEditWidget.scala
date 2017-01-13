package se.lu.nateko.cp.doi.gui.views

import se.lu.nateko.cp.doi.meta.Title
import scalatags.JsDom.all._
import scala.collection.mutable.Buffer
import org.scalajs.dom.Event
import org.scalajs.dom.html
import scalatags.JsDom.TypedTag

class TitlesEditWidget(init: Seq[Title], cb: Seq[Title] => Unit){

	private val titles = init.toBuffer
	private val widgets = Buffer.empty[TitleWidget]

	private def setRemovability(): Unit = if(titles.length == 1){
		widgets(0).setRemovability(false)
	} else widgets.foreach(_.setRemovability(true))

	private def removeTitle(idx: Int): Unit = {
		titles.remove(idx)
		val toRemove = widgets(idx).element
		toRemove.parentElement.removeChild(toRemove)
		widgets.remove(idx)
		setRemovability()
		cb(titles)
	}

	titles.zipWithIndex.foreach{case (title, idx) =>
		widgets += new TitleWidget(
			title,
			t => {titles(idx) = t; cb(titles)},
			() => {removeTitle(idx); cb(titles)}
		)
	}
	setRemovability()

	val element = Bootstrap.basicPanel(
		Bootstrap.propValueRow(
			span("Titles"),
			span(cls := "glyphicon glyphicon-plus")
		)(widgets.map(_.element))
	)

}
