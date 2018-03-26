package tech.bitmin.circlemenu

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import tech.bitmin.circlemunu.CircleMenuContract


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dataList = ArrayList<CircleMenuContract.Model.ItemData>()
        for (i in 1..4) {
            val data = CircleMenuContract.Model.ItemData().setImageBitmap(this, R.drawable.ic_launcher_round).setTitleResId(this, R.string.app_name)
            dataList.add(data)
        }
        circleMenu.mPresenter.setOnItemClickListener { toastItem(it) }
                .setOnCenterItemClickListener { toastCenter() }
                .addItems(dataList)
                .invalidate()
    }

    private fun toastCenter() {
        Toast.makeText(applicationContext, "center", Toast.LENGTH_SHORT).show()
    }

    private fun toastItem(position: Int) {
        Toast.makeText(applicationContext, "item: " + position, Toast.LENGTH_SHORT).show()
    }
}