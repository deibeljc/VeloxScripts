package scripts.behaviortree

// Define the result types for node execution
enum class BehaviorTreeStatus {
  SUCCESS,
  FAILURE,
  RUNNING,
  KILL
}

// Interface for all nodes in the behavior tree
interface IBehaviorNode {
  val label: String?
  val children: List<IBehaviorNode>
  
  // Add a read-only property for status
  val status: BehaviorTreeStatus

  // Change tick() to return Unit and update status
  fun tick()
}

// Base class for parent nodes
abstract class IParentNode(override val label: String? = null) : IBehaviorNode {
  protected val _children = mutableListOf<IBehaviorNode>()
  override val children: List<IBehaviorNode>
    get() = _children

  // Add status property
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.RUNNING
    protected set

  fun addChild(node: IBehaviorNode) {
    _children.add(node)
  }
}

// Sequence node: succeeds if all children succeed
class SequenceNode(label: String? = null) : IParentNode(label) {
  override fun tick() {
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
class ConditionNode(override val label: String? = null, private val condition: () -> Boolean) : IBehaviorNode {
  override val children: List<IBehaviorNode> = emptyList()
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.RUNNING
    private set

  override fun tick() {
    status = if (condition()) BehaviorTreeStatus.SUCCESS else BehaviorTreeStatus.FAILURE
  }
}

// Perform node: executes an action and always returns success
class PerformNode(override val label: String? = null, private val action: () -> Unit) : IBehaviorNode {
  override val children: List<IBehaviorNode> = emptyList()
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.RUNNING
    private set

  override fun tick() {
    action()
    status = BehaviorTreeStatus.SUCCESS
  }
}

// Conditional decorator node: runs child if condition is true
class ConditionalNode(private val condition: () -> Boolean, private val child: IBehaviorNode) : IBehaviorNode {
  override val label: String = "Conditional"
  override val children: List<IBehaviorNode> = listOf(child)
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.RUNNING
    private set

  override fun tick() {
    if (condition()) {
      child.tick()
      status = child.status
    } else {
      status = BehaviorTreeStatus.FAILURE
    }
  }
}

// RepeatUntil node: repeats child until a certain status is returned
class RepeatUntilNode(
  private val stopStatus: () -> Boolean,
  private val child: IBehaviorNode
) : IBehaviorNode {
  override val label: String = "RepeatUntil"
  override val children: List<IBehaviorNode> = listOf(child)
  override var status: BehaviorTreeStatus = BehaviorTreeStatus.RUNNING
    private set

  override fun tick() {
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
