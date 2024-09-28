package scripts.behaviortree

// Define the result types for node execution
enum class BehaviorTreeStatus {
  SUCCESS,
  FAILURE,
  RUNNING,
  KILL,
  IDLE // New status
}

// Interface for all nodes in the behavior tree
interface IBehaviorNode {
  val label: String?
  val children: List<IBehaviorNode>

  // Change the default status to IDLE
  val status: BehaviorTreeStatus

  fun tick()

  // Add a method to reset the node status
  fun reset()
}

// Base class for parent nodes
abstract class IParentNode(override val label: String? = null) : IBehaviorNode {
  protected val _children = mutableListOf<IBehaviorNode>()
  override val children: List<IBehaviorNode>
    get() = _children

  // Change initial status to IDLE
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.IDLE
    protected set

  // Implement reset method
  override fun reset() {
    status = BehaviorTreeStatus.IDLE
    _children.forEach { it.reset() }
  }

  fun addChild(node: IBehaviorNode) {
    _children.add(node)
  }
}

// Sequence node: succeeds if all children succeed
class SequenceNode(label: String? = null) : IParentNode(label) {
  override fun tick() {
    status = BehaviorTreeStatus.RUNNING
    for (child in children) {
      child.tick()
      if (child.status != BehaviorTreeStatus.SUCCESS) {
        status = child.status
        return
      }
    }
    status = BehaviorTreeStatus.SUCCESS
  }
}

// Selector node: succeeds if any child succeeds
class SelectorNode(label: String? = null) : IParentNode(label) {
  override fun tick() {
    status = BehaviorTreeStatus.RUNNING
    for (child in children) {
      child.tick()
      if (child.status == BehaviorTreeStatus.SUCCESS) {
        status = BehaviorTreeStatus.SUCCESS
        return
      } else if (child.status == BehaviorTreeStatus.KILL) {
        status = BehaviorTreeStatus.KILL
        return
      }
    }
    status = BehaviorTreeStatus.FAILURE
  }
}

// Condition node: evaluates a boolean condition
class ConditionNode(override val label: String? = null, private val condition: () -> Boolean) :
    IBehaviorNode {
  override val children: List<IBehaviorNode> = emptyList()
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.IDLE
    private set

  override fun reset() {
    status = BehaviorTreeStatus.IDLE
  }

  override fun tick() {
    status = BehaviorTreeStatus.RUNNING
    status = if (condition()) BehaviorTreeStatus.SUCCESS else BehaviorTreeStatus.FAILURE
  }
}

// Perform node: executes an action and always returns success
class PerformNode(override val label: String? = null, private val action: () -> Unit) :
    IBehaviorNode {
  override val children: List<IBehaviorNode> = emptyList()
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.IDLE
    private set

  override fun reset() {
    status = BehaviorTreeStatus.IDLE
  }

  override fun tick() {
    status = BehaviorTreeStatus.RUNNING
    action()
    status = BehaviorTreeStatus.SUCCESS
  }
}

// Conditional decorator node: runs child if condition is true
class ConditionalNode(private val condition: () -> Boolean, private val child: IBehaviorNode) :
    IBehaviorNode {
  override val label: String = "Conditional"
  override val children: List<IBehaviorNode> = listOf(child)
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.IDLE
    private set

  override fun reset() {
    status = BehaviorTreeStatus.IDLE
    child.reset()
  }

  override fun tick() {
    status = BehaviorTreeStatus.RUNNING
    if (condition()) {
      child.tick()
      status = child.status
    } else {
      status = BehaviorTreeStatus.FAILURE
    }
  }
}

// RepeatUntil node: repeats child until a certain status is returned
class RepeatUntilNode(private val stopStatus: () -> Boolean, private val child: IBehaviorNode) :
    IBehaviorNode {
  override val label: String = "RepeatUntil"
  override val children: List<IBehaviorNode> = listOf(child)
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.IDLE
    private set

  override fun reset() {
    status = BehaviorTreeStatus.IDLE
    child.reset()
  }

  override fun tick() {
    status = BehaviorTreeStatus.RUNNING
    while (true) {
      child.tick()
      if (stopStatus() || child.status == BehaviorTreeStatus.KILL) {
        status = child.status
        return
      }
    }
  }
}

// BehaviorTree class to build and execute the tree
class BehaviorTree(private val root: IBehaviorNode) {
  fun tick() {
    root.tick()
  }

  fun reset() {
    root.reset()
  }

  fun root(): IBehaviorNode {
    return root
  }
}

// Helper functions to build the tree using DSL-like syntax
fun behaviorTree(label: String? = null, init: IParentNode.() -> Unit): BehaviorTree {
  val root = SequenceNode(label)
  root.init()
  return BehaviorTree(root)
}

fun IParentNode.sequence(label: String? = null, init: IParentNode.() -> Unit) {
  val node = SequenceNode(label)
  node.init()
  addChild(node)
}

fun IParentNode.selector(label: String? = null, init: IParentNode.() -> Unit) {
  val node = SelectorNode(label)
  node.init()
  addChild(node)
}

fun IParentNode.condition(label: String? = null, predicate: () -> Boolean) {
  addChild(ConditionNode(label, predicate))
}

fun IParentNode.perform(label: String? = null, action: () -> Unit) {
  addChild(PerformNode(label, action))
}

fun IParentNode.conditional(initCondition: () -> Boolean, init: IParentNode.() -> Unit) {
  val childNode = SequenceNode()
  childNode.init()
  addChild(ConditionalNode(initCondition, childNode))
}

fun IParentNode.repeatUntil(stopStatus: () -> Boolean, init: IParentNode.() -> Unit) {
  val childNode = SequenceNode()
  childNode.init()
  addChild(RepeatUntilNode(stopStatus, childNode))
}
