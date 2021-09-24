package com.example.fragmentknowledge

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE

class MainActivity : AppCompatActivity() {
    private var index = 0
    private var isAllowStateLoss = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().apply {
            add(R.id.frameLayout, OneFragment(), OneFragment().javaClass.name)
        }.commit()

        findViewById<Button>(R.id.buttonNext).setOnClickListener {
            next(false)
        }

        findViewById<Button>(R.id.buttonNextWithReordering).setOnClickListener {
            nextWithReordering()
        }

        findViewById<Button>(R.id.buttonPopBackStack).setOnClickListener {
            popBackStack()
        }

        findViewById<Button>(R.id.buttonPopBackStackInclusive).setOnClickListener {
            popBackStackInclusive()
        }

        findViewById<Button>(R.id.buttonPopBackStackNotInclusive).setOnClickListener {
            popBackStackNotInclusive()
        }

        findViewById<Button>(R.id.normalCommit).setOnClickListener {
            isAllowStateLoss = false
            this@MainActivity.startActivity(Intent(this@MainActivity, AnotherActivity::class.java))
        }

        findViewById<Button>(R.id.allowStateLossCommit).setOnClickListener {
            isAllowStateLoss = true
            this@MainActivity.startActivity(Intent(this@MainActivity, AnotherActivity::class.java))
        }

        findViewById<Button>(R.id.nextNowWithAddBackStack).setOnClickListener {
            nextNowWithAddBackStack()
        }
    }

    private fun next(isAllowStateLoss: Boolean) {
        if (index == 3)
            return
        index++
        val fragment = when (index) {
            0 -> OneFragment()
            1 -> TwoFragment()
            2 -> ThreeFragment()
            else -> FourFragment()
        }
        val transaction = supportFragmentManager.beginTransaction().apply {
            add(R.id.frameLayout, fragment, fragment.javaClass.name)
            addToBackStack(fragment.javaClass.name)
        }

        if(isAllowStateLoss)
            transaction.commitAllowingStateLoss()
        else
            transaction.commit()
    }

    private fun nextWithReordering() {
        if (index == 3)
            return
        index++
        val fragment = when (index) {
            0 -> OneFragment()
            1 -> TwoFragment()
            2 -> ThreeFragment()
            else -> FourFragment()
        }
        //setReorderingAllowed(true) => combine all actions
        // => optimize all actions to be come more less action with same result
        //for example : all action bellow become => add(fragment)
        supportFragmentManager.beginTransaction().apply {
            add(R.id.frameLayout, OneFragment(), fragment.javaClass.name)
            replace(R.id.frameLayout, TwoFragment(), fragment.javaClass.name)
            replace(R.id.frameLayout, ThreeFragment(), fragment.javaClass.name)
            replace(R.id.frameLayout, fragment, fragment.javaClass.name)
            setReorderingAllowed(true)
            addToBackStack(fragment.javaClass.name)
        }.commit()
    }

    private fun nextNowWithAddBackStack() {
        if (index == 3)
            return
        index++
        val fragment = when (index) {
            0 -> OneFragment()
            1 -> TwoFragment()
            2 -> ThreeFragment()
            else -> FourFragment()
        }
        // commit now with addToBackStack will be throw Exception
        // Android disallow add to back stack inside commitNow
        // Because commit now will excute immediately, so if add back stack inside this function
        // the orderding of the stack can be incorrect if the transaction before is used commit()
        // commit() is asynchronous , it not excute immediately like commit now
        supportFragmentManager.beginTransaction().apply {
            add(R.id.frameLayout, fragment, fragment.javaClass.name)
            addToBackStack(fragment.javaClass.name)
        }.commitNow()
    }

    private fun popBackStack() {
        // stack 1 - 2 - 3 - 4  => popstack()  = pop [4] => 1 - 2 - 3
        if (supportFragmentManager.backStackEntryCount == 0)
            return
        supportFragmentManager.popBackStack()
        index = supportFragmentManager.backStackEntryCount - 1
    }

    private fun popBackStackInclusive() {
        // ID or NAME are the same
        // stack 1 - 2 - 3 - 4  => popstack(nameOrID[3], Inclusive)  = pop [4, 3] => 1 - 2
        if (supportFragmentManager.backStackEntryCount == 0)
            return
        supportFragmentManager.run {
            popBackStack(
                getBackStackEntryAt(backStackEntryCount - 1).id,
                POP_BACK_STACK_INCLUSIVE
            )
        }
        index = supportFragmentManager.backStackEntryCount - 1
    }

    private fun popBackStackNotInclusive() {
        // ID or NAME are the same
        // stack 1 - 2 - 3 - 4  => popstack(nameOrID[4])  = pop [nothing] => 1 - 2 - 3 - 4
        // stack 1 - 2 - 3 - 4  => popstack(nameOrID[3])  = pop [4] => 1 - 2 - 3

        if (supportFragmentManager.backStackEntryCount == 1) {
            popBackStack()
        } else if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.run {
                popBackStack(
                    getBackStackEntryAt(backStackEntryCount - 2).id,
                    0
                )
            }
        }
        index = supportFragmentManager.backStackEntryCount - 1
    }

    override fun onStop() {
        super.onStop()
        next(isAllowStateLoss)
    }
}