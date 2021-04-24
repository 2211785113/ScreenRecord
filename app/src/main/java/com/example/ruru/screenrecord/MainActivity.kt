package com.example.ruru.screenrecord

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.example.ruru.screenrecord.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        //在子线程中启动录屏(废弃)
        btn_runnable_start.setOnClickListener {
//            startActivity(Intent(this, MainThreadAct::class.java))
        }

        //以服务方式启动录屏
        btn_service_start.setOnClickListener {
            startActivity(Intent(this, MainServiceAct::class.java))
        }
    }
}