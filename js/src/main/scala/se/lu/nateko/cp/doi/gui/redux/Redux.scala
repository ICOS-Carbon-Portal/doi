package se.lu.nateko.cp.doi.gui.redux

import scala.collection.mutable.Buffer

trait Redux{

	type State
	type Action
	type Reducer = Function2[Action, State, State]

	trait StateListener{
		def notify(newState: State, oldState: State): Unit
	}

	trait Dispatcher{
		def getState: State
		def dispatch(action: ThunkAction): Unit
		def dispatch(action: Action): Unit
	}

	type ThunkAction = Function1[Dispatcher, Unit]

	class Store(reducer: Reducer, init: State) extends Dispatcher{

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
			subscribers.foreach(s => schedule(s.notify(state, oldState)))
		}

		def dispatch(thunk: ThunkAction): Unit = schedule(thunk(this))

		private def schedule(work: => Unit): Unit = scala.scalajs.concurrent.JSExecutionContext
			.Implicits.queue.execute(
				new Runnable{
					override def run(): Unit = work
				}
			)
	}
}



