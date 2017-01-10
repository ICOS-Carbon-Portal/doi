package se.lu.nateko.cp.doi.gui.redux

import scala.collection.mutable.Buffer
import scala.concurrent.ExecutionContext

trait Redux{
	type State
	type Action
	type Reducer = Function2[Action, State, State]

	trait StateListener{
		def notify(action: Action, newState: State, oldState: State): Unit
	}

	class Store(reducer: Reducer, init: State)(implicit val exeContext: ExecutionContext){

		private[this] val subscribers = Buffer.empty[StateListener]
		private[this] var state : State = init

		def getState: State = state

		def subscribe(listener: StateListener): Boolean = {
			if(subscribers.indexOf(listener) < 0) {
				subscribers += listener
				true
			} else false
		}

		def unsubscribe(listener: StateListener): Boolean = {
			val idx = subscribers.indexOf(listener)
			if(idx >= 0) {
				subscribers.remove(idx)
				true
			} else false
		}

		def dispatch(action: Action): Unit = schedule{
			val oldState = state
			state = reducer(action, oldState)
			subscribers.foreach(s => schedule(s.notify(action, state, oldState)))
		}

		private def schedule(work: => Unit): Unit = exeContext.execute(new Runnable{
			override def run(): Unit = work
		})
	}
}



