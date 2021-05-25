package com.rockspin.rxredux.dsl

import com.rockspin.rxredux.core.*
import io.reactivex.Observable


/**
 * Use the builder pattern to create an epic
 */
fun <A : Action, S : State> buildEpic(label:String, builder: EpicBuilder<A, S>.() -> Unit): Epic<A, S> {
    val epicBuilder = EpicBuilder<A, S>()
    builder(epicBuilder)
    return epicBuilder.build(label)
}

/** Modify the epic parameters*/
fun <A : Action, /*NA : A, */S : State/*, NS : S*/> Epic<A, S>.modifyEpicParams(
        modifyActions: Observable<A>.() -> Observable<A>
//        modifyState: Observable<S>.() -> Observable<S>
): Epic<A, S> = { (actions, state) ->
    this(EpicParams(modifyActions(actions), /*modifyState(*/state/*)*/))
}

/** Modify the epics output observable */
fun <A : Action, S : State> Epic<A, S>.transformOutput(
        transform: Observable<Action>.() -> Observable<Action>
): Epic<A, S> = { (actions, state) ->
    transform(this(EpicParams(actions, state)))
}

@DslMarker
annotation class ActionBuilderMarker

@ActionBuilderMarker
class EpicBuilder<A : Action, S : State> internal constructor() {

    private var actionCreators: MutableList<Epic<A, S>> = mutableListOf()

    fun withEpic(epic: Epic<A, S>) {
        actionCreators.add(epic)
    }

    /** Add an epic which filters the actions observable by type
     * If an epic has multiple children, it should set @param [hasMultipleChildren] so that the observable is shared*/
    @JvmName("withSubActionEpic")
    inline fun <reified NA : A> withSubEpic(
            crossinline epic: Epic<NA, S>,
            hasMultipleChildren: Boolean = false
    ) {
        withEpic { (actions: Observable<A>, state: Observable<S>) ->
            val a = actions.ofTypeSafeLoggedd<NA>().run{
                if (hasMultipleChildren) share() else this
            }
            epic(EpicParams(a, state))
        }
    }

    /** Add an epic which maps the state observable */
    @JvmName("withSubStateEpic")
    inline fun <reified NS : State> withSubEpic(
            crossinline epic: Epic<A, NS>,
            crossinline mapState: (S) -> NS
    ) {
        withEpic { (actions: Observable<A>, state: Observable<S>) ->
            val s = state.map { mapState(it) }
            epic(EpicParams(actions, s))
        }
    }

    /** Add an epic which filters the actions observable by type and maps the state observable */
    inline fun <reified NA : A, reified NS : State> withSubEpic(
            crossinline epic: Epic<NA, NS>,
            crossinline mapState: (S) -> NS,
            hasMultipleChildren: Boolean = false
    ) {
        withEpic { (actions: Observable<A>, state: Observable<S>) ->
            val a = actions.ofTypeSafeLoggedd<NA>().run {
                if (hasMultipleChildren) share() else this
            }
            val s = state.map { mapState(it) }
            epic(EpicParams(a, s))
        }
    }

    fun build(label:String): Epic<A, S> = combineEpics(label, *actionCreators.toTypedArray())
}

//TODO Remove
inline fun <reified R : Any> Observable<in R>.ofTypeSafeLoggedd(): Observable<R> = ofTypeLoggedd()

inline fun <reified R : Any> Observable<*>.ofTypeLoggedd(): Observable<R> {
    val classss = R::class.java
    return filter {
        val isInstance = classss.isInstance(it)
//        Timber.w("ofTypeLogged(Epic): Checking if ${it::class.simpleName} is ${classss.simpleName} - result: $isInstance")
        isInstance
    }.cast(classss)
}