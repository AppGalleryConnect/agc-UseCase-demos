package com.example.xuankuapp.Activity

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.xuankuapp.R
import com.huawei.agconnect.function.AGCFunctionException
import com.huawei.agconnect.function.AGConnectFunction
import com.huawei.agconnect.remoteconfig.AGConnectConfig
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * KotlinActivity test the kotlin function of AGC remote configuration and cloud functions.
 *
 * @since 2022-08-02
 * <p>
 * Copyright (c) HuaWei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */
class KotlinActivity : AppCompatActivity(){

    private val GREETING_KEY = "GREETING_KEY"
    private val SET_BOLD_KEY = "SET_BOLD_KEY"
    private var config: AGConnectConfig? = null
    private var tv_greeting: TextView? = null
    private val TAG = "XuankuAPP"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin)

        /**
         * GetOnlineConfig
         */
        config = AGConnectConfig.getInstance()
        config!!.applyDefault(R.xml.remote_config)
        tv_greeting = findViewById(R.id.tv_1)
        tv_greeting!!.setText(config!!.getValueAsString(GREETING_KEY))
        val isBold = config!!.getValueAsBoolean(SET_BOLD_KEY)
        if (isBold) {
            tv_greeting!!.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        }
        val btn_config:Button = findViewById(R.id.bt_1)
        btn_config.setOnClickListener { fetchAndApply() }

        /**
         * CloudFunction
         */
        val btn_Cloud = findViewById<Button>(R.id.bt_2)
        val et_year = findViewById<EditText>(R.id.et_1)
        btn_Cloud.setOnClickListener(View.OnClickListener {
            val inputText = et_year.text.toString()
            if (inputText == "" || !isInputLegit(inputText)) {
                tv_greeting!!.text = "The entered year is incorrect."
                return@OnClickListener
            }
            val function = AGConnectFunction.getInstance()
            val map = HashMap<String, String>()
            map["year"] = inputText
            function.wrap("testfunction-\$latest").call(map)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val value = task.result.value
                        try {
                            val `object` = JSONObject(value)
                            val result = `object`["result"] as String
                            tv_greeting!!.text = result
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        Log.i(TAG, value)
                    } else {
                        val e = task.exception
                        if (e is AGCFunctionException) {
                            val functionException = e
                            val errCode = functionException.code
                            val message = functionException.message
                            Log.e(
                                TAG,
                                "errorCode: $errCode, message: $message"
                            )
                        }
                    }
                }
        })

    }

    private fun fetchAndApply() {
        config!!.fetch(0).addOnSuccessListener { configValues ->
            config!!.apply(configValues)
            updateUI()
        }.addOnFailureListener { e -> tv_greeting!!.text = "fetch setting failed:" + e.message }
    }

    private fun updateUI() {
        val text = config!!.getValueAsString(GREETING_KEY)
        val isBold = config!!.getValueAsBoolean(SET_BOLD_KEY)
        tv_greeting!!.text = text
        if (isBold) {
            tv_greeting!!.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD))
        }
    }

    private fun isInputLegit(input: String): Boolean {
        for (i in 0 until input.length) {
            println(input[i])
            if (!Character.isDigit(input[i])) {
                return false
            }
        }
        return true
    }
}