package components

import core.{Component, ComponentSystem}

class TestComponent(name: String, var setting: String)(implicit componentSystem: ComponentSystem)
  extends Component(name) {
  override protected def _getValue: () => String = () => setting
  override protected def _onReload: () => Unit = () => println(s"Reloaded TestComponent '$name'")
  override protected def _onChange: String => Unit = (newValue: String) => setting = newValue
}
