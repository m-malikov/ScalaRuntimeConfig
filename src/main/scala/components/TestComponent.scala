package components

import core.Component

class TestComponent(name: String, var setting: String) extends Component(name) {
  override protected def _getValue: () => String = () => setting
  override protected def _onReload: () => Unit = () => println(s"Reloaded TestComponent '$name'")
  override protected def _onChange: String => Unit = (newValue: String) => setting = newValue
}
