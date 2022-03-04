package com.qpsoft.checkrender.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.qpsoft.checkrender.R
import com.qpsoft.checkrender.common.Helper
import com.qpsoft.checkrender.data.model.ComboItem
import com.qpsoft.checkrender.databinding.FooterUpFilesBinding
import com.qpsoft.checkrender.databinding.FragmentMainBinding
import com.qpsoft.checkrender.utils.glide.GlideEngine
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject


@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java).apply {
            comboItemListLiveData.observe(this@MainFragment, {
                comboItemList = it
            })
        }

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_main, null, false)

        viewModel.comboItemList()
    }

    private lateinit var binding: FragmentMainBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }


    private var comboItemList = mutableListOf<ComboItem>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getCheckItemData()

        binding.btn.setOnClickListener {
            renderUI(comboItemList)
        }

        binding.submit.setOnClickListener {
            submitData(comboItemList)
        }
    }

    private val checkItemObj: JSONObject = JSONObject()
    private fun getCheckItemData() {
        val obj1 = JSONObject()


        val obj11 = JSONObject()

        obj11.put("远", "2222")
        obj11.put("近", "11111")
        obj11.put("抑制/复现", "复现")


        val obj22 = JSONObject()
        obj22.put("OD", "5")
        obj22.put("OS", "6")
        obj22.put("OU", "88")
        obj22.put("方法", "移近法")

        val obj33 = JSONObject()
        obj33.put("key0", "15")
        obj33.put("key1", "16")
        obj33.put("方法", "48")

        obj1.put("主导眼", "左眼")
        obj1.put("WORTH 4 DOT", obj11)
        obj1.put("调节幅度(AMP)", obj22)
        obj1.put("集合近点NPC", obj33)
        obj1.put("BCC", "333")
        val obj44 = JSONObject()
        obj44.put("key0", "15")
        obj44.put("key1", "16")
        obj44.put("方法", "箱灯")
        obj1.put("集合近点NPC", obj44)
        obj1.put("调节功能", "333")

        val obj55 = JSONObject()
        val obj551 = JSONObject()
        obj551.put("key0", "1")
        obj551.put("key1", "2")
        obj55.put("远距离水平斜位检查", obj551)

        val obj66 = JSONObject()
        val obj553 = JSONObject()
        obj553.put("key0", "1")
        obj553.put("key1", "2")
        obj553.put("key2", "3")
        obj66.put("BI", obj553)
        val obj554 = JSONObject()
        obj554.put("key0", "1")
        obj554.put("key1", "2")
        obj66.put("BO", obj554)
        obj55.put("远距离水平聚散力检查", obj66)
        obj1.put("立体视", obj55)

        //checkItemObj.put(comboItemStr, obj1)
        LogUtils.e("------$checkItemObj")
    }


    private val comboItemStr = "电脑验光"
    private fun renderUI(list: MutableList<ComboItem>) {
        val comboItem = list.find { it.name == comboItemStr } ?: return
        when(comboItem.category) {
            "normal", -> showNormal(comboItem)
            "ok" -> showOk(comboItem)
            "special" -> showSpecial(comboItem)
        }
        if (comboItem.fileModule) {
            val childView = layoutInflater.inflate(R.layout.view_files, binding.rootView, false)
            binding.rootView.addView(childView)

            val picList = mutableListOf<String>()
            val picAdapter = object: BaseQuickAdapter<String, BaseViewHolder>(R.layout.rv_files_item, picList) {
                override fun convert(holder: BaseViewHolder, item: String) {
                    val ivFile = holder.getView<ImageView>(R.id.iv_file)
                    Helper.loadFile(ivFile, item)
                }
            }
            val rvFiles = childView.findViewById<RecyclerView>(R.id.rv_files)
            rvFiles.layoutManager = GridLayoutManager(requireContext(), 3)
            rvFiles.adapter = picAdapter

            //footer layout
            val footerView = layoutInflater.inflate(R.layout.footer_up_files, binding.rootView, false)
            picAdapter.setFooterView(footerView)
            val footerUpFilesBinding = DataBindingUtil.bind<FooterUpFilesBinding>(footerView)!!
            footerUpFilesBinding.ivUpFiles.setOnClickListener {
                //cameraRequester.launch()
                PictureSelector.create(this@MainFragment)
                    .openGallery(PictureMimeType.ofImage())
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    .imageEngine(GlideEngine.createGlideEngine())
                    .maxSelectNum(1)
                    .isCompress(true)
                    .isEnableCrop(false)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
            }
        }
        if (comboItem.resultModule) {
            val childView = layoutInflater.inflate(R.layout.view_result, binding.rootView, false)
            binding.rootView.addView(childView)
        }
        if (comboItem.remarkModule) {
            val childView = layoutInflater.inflate(R.layout.view_remark, binding.rootView, false)
            binding.rootView.addView(childView)
        }
    }

    private fun showNormal(comboItem: ComboItem) {
        val showEyes: Boolean = comboItem.doubleEye
        val itemList = comboItem.items

        val childView: LinearLayout
        if (showEyes) {
            childView = layoutInflater.inflate(R.layout.view_odos, binding.rootView, false) as LinearLayout
        } else {
            childView = layoutInflater.inflate(R.layout.view_no_odos, binding.rootView, false) as LinearLayout
            val tvTitle = childView.findViewById<TextView>(R.id.tv_title)
            tvTitle.text = comboItem.name
        }

        var pos = -1
        for (item in itemList) {
            if (item.type == "vision" || item.type == "sph" || item.type == "cyl" || item.type == "axis"
                || item.type == "iop" || item.type == "pd" || item.type == "add" || item.type == "al") {
                val itemView: View
                if (item.doubleEye) {
                    itemView = layoutInflater.inflate(R.layout.view_spe_odos, null)
                    val tvNameOd = itemView.findViewById<TextView>(R.id.tv_name_od)
                    val tvNameOs = itemView.findViewById<TextView>(R.id.tv_name_os)
                    tvNameOd.text = item.name
                    tvNameOs.text = item.name

                    val edtValueOd = itemView.findViewById<EditText>(R.id.edt_value_od)
                    val edtValueOs = itemView.findViewById<EditText>(R.id.edt_value_os)
                    edtValueOd.tag = item.item+item.key+"od"
                    edtValueOs.tag = item.item+item.key+"os"

                    //show data
                    if (!checkItemObj.isNull(item.item)) {
                        edtValueOd.setText(checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString("od"))
                        edtValueOs.setText(checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString("os"))
                    }
                } else {
                    itemView = layoutInflater.inflate(R.layout.view_spe_no_odos, null)
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    tvName.text = item.name

                    val edtValue = itemView.findViewById<EditText>(R.id.edt_value)
                    edtValue.tag = item.item+item.key

                    //show data
                    if (!checkItemObj.isNull(item.item)) {
                        edtValue.setText(checkItemObj.getJSONObject(item.item).getString(item.key))
                    }
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_9fc))
                }
            }
            if (item.type == "radio") {
                val itemView: View
                if (item.doubleEye) {
                    itemView = layoutInflater.inflate(R.layout.view_radio_odos, null)
                    val tvNameOd = itemView.findViewById<TextView>(R.id.tv_name_od)
                    val tvNameOs = itemView.findViewById<TextView>(R.id.tv_name_os)
                    tvNameOd.text = item.name
                    tvNameOs.text = item.name

                    val rgOdView = itemView.findViewById<RadioGroup>(R.id.rg_od)
                    for (str in item.optionList!!) {
                        val radioBtn = layoutInflater.inflate(R.layout.view_rb, null) as RadioButton
                        radioBtn.text = str
                        rgOdView.addView(radioBtn)
                    }
                    rgOdView.tag = item.item + item.key+"od"

                    //show data
                    if (!checkItemObj.isNull(item.item)) {
                        if (!checkItemObj.getJSONObject(item.item).isNull(item.key)) {
                            for (ii in 0 until rgOdView.childCount) {
                                val rb = rgOdView[ii] as RadioButton
                                if (rb.text.toString() == checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString("od")) {
                                    rb.isChecked = true
                                }
                            }
                        }
                    }

                    val rgOsView = itemView.findViewById<RadioGroup>(R.id.rg_os)
                    for (str in item.optionList!!) {
                        val radioBtn = layoutInflater.inflate(R.layout.view_rb, null) as RadioButton
                        radioBtn.text = str
                        rgOsView.addView(radioBtn)
                    }
                    rgOsView.tag = item.item + item.key+"os"

                    //show data
                    if (!checkItemObj.isNull(item.item)) {
                        if (!checkItemObj.getJSONObject(item.item).isNull(item.key)) {
                            for (ii in 0 until rgOsView.childCount) {
                                val rb = rgOsView[ii] as RadioButton
                                if (rb.text.toString() == checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString("os")) {
                                    rb.isChecked = true
                                }
                            }
                        }
                    }
                } else {
                    itemView = layoutInflater.inflate(R.layout.view_radio_no_odos, null)
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    tvName.text = item.name
                    val rgView = itemView.findViewById<RadioGroup>(R.id.rg)
                    for (str in item.optionList!!) {
                        val radioBtn = layoutInflater.inflate(R.layout.view_rb, null) as RadioButton
                        radioBtn.text = str
                        rgView.addView(radioBtn)
                    }
                    rgView.tag = item.item + item.key

                    //show data
                    if (!checkItemObj.isNull(item.item)) {
                        if (!checkItemObj.getJSONObject(item.item).isNull(item.key)) {
                            for (ii in 0 until rgView.childCount) {
                                val rb = rgView[ii] as RadioButton
                                if (rb.text.toString() == checkItemObj.getJSONObject(item.item).getString(item.key)) {
                                    rb.isChecked = true
                                }
                            }
                        }
                    }
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_9fc))
                }
            }

            if (item.type == "array") {
                val itemView = layoutInflater.inflate(R.layout.view_array_no_odos, null) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                tvName.text = item.name

                val arrayView = itemView.findViewById<LinearLayout>(R.id.ll_array)

                val iList = item.itemList
                if (iList != null) {
                    for(it in iList) {
                        if (it.type == "select") {
                            val sView = layoutInflater.inflate(R.layout.view_array_select_no_odos, null)
                            val tvName = sView.findViewById<TextView>(R.id.tv_name)
                            tvName.text = it.name

                            val tvValue = sView.findViewById<TextView>(R.id.tv_value)
                            tvValue.tag = item.key+it.key

                            //show data
                            if (!checkItemObj.isNull(item.item)) {
                                if (!checkItemObj.getJSONObject(item.item).getJSONObject(item.key).isNull(it.key)) {
                                    tvValue.text = checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString(it.key)
                                }
                            }

                            arrayView.addView(sView)

                            onSelectHandle(tvValue, it.optionList)
                        }
                        if (it.type == "template") {
                            val llTemplate = layoutInflater.inflate(R.layout.view_array_template_no_odos, null) as LinearLayout

                            val tvName = llTemplate.findViewById<TextView>(R.id.tv_name)
                            tvName.text = it.name

                            val str = it.template
                            val separator = str.substring(0,1)
                            val count = str.substring(1).split(separator).size
                            for (i in 0 until count){
                                val llContent = layoutInflater.inflate(R.layout.view_template_content, null)
                                val tvSeparator = llContent.findViewById<TextView>(R.id.tv_separator)
                                tvSeparator.text = separator

                                val edtValue = llContent.findViewById<EditText>(R.id.edt_value)
                                edtValue.tag = item.key+it.key+"key$i"

                                //show data
                                if (!checkItemObj.isNull(item.item)) {
                                    if (!checkItemObj.getJSONObject(item.item).isNull(item.key)) {
                                        if (!checkItemObj.getJSONObject(item.item).getJSONObject(item.key).isNull(it.key)) {
                                            edtValue.setText(checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getJSONObject(it.key).getString("key$i"))
                                        }
                                    }
                                }

                                llTemplate.addView(llContent)
                            }

                            arrayView.addView(llTemplate)
                        }

                        if (it.type == "array") {
                            val aaView = layoutInflater.inflate(R.layout.view_array_array_no_odos, null) as LinearLayout
                            val tvName = aaView.findViewById<TextView>(R.id.tv_name)
                            tvName.text = it.name

                            val array2View = aaView.findViewById<LinearLayout>(R.id.ll_array)

                            val iList2 = it.itemList
                            if (iList2 != null) {
                                for(it2 in iList2) {
                                    if (it2.type == "template") {
                                        val llTemplate = layoutInflater.inflate(R.layout.view_array_array_template_no_odos, null) as LinearLayout

                                        val tvName = llTemplate.findViewById<TextView>(R.id.tv_name)
                                        tvName.text = it2.name


                                        val str = it2.template
                                        val separator = str.substring(0,1)
                                        val count = str.substring(1).split(separator).size
                                        for (i in 0 until count){
                                            val llContent = layoutInflater.inflate(R.layout.view_template_content, null)
                                            val tvSeparator = llContent.findViewById<TextView>(R.id.tv_separator)
                                            tvSeparator.text = separator

                                            val edtValue = llContent.findViewById<EditText>(R.id.edt_value)
                                            edtValue.tag = it.key+it2.key+"key$i"

                                            //show data
                                            if (!checkItemObj.isNull(item.item)) {
                                                if (!checkItemObj.getJSONObject(item.item).isNull(item.key)) {
                                                    if (!checkItemObj.getJSONObject(item.item).getJSONObject(item.key).isNull(it.key)) {
                                                        if (!checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getJSONObject(it.key).isNull(it2.key)) {
                                                            edtValue.setText(checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getJSONObject(it.key).getJSONObject(it2.key).getString("key$i"))
                                                        }
                                                    }
                                                }
                                            }

                                            llTemplate.addView(llContent)
                                        }

                                        array2View.addView(llTemplate)
                                    }

                                }
                            }
                            arrayView.addView(aaView)
                        }

                        if (it.type == "text") {
                            val txtView = layoutInflater.inflate(R.layout.view_array_text_no_odos, null)
                            val tvName = txtView.findViewById<TextView>(R.id.tv_name)
                            tvName.text = it.name

                            val edtValue = txtView.findViewById<EditText>(R.id.edt_value)
                            edtValue.tag = item.key+it.key

                            //show data
                            if (!checkItemObj.isNull(item.item)) {
                                if (!checkItemObj.getJSONObject(item.item).isNull(item.key)) {
                                    edtValue.setText(checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString(it.key))
                                }
                            }

                            arrayView.addView(txtView)
                        }
                    }
                }

                if (item.suffix != null) {
                    val suffixItem = item.suffix
                    if (suffixItem.type == "radio") {
                        val suffixView = layoutInflater.inflate(R.layout.view_suffix_radio_no_odos, null)
                        val tvName = suffixView.findViewById<TextView>(R.id.tv_name)
                        tvName.text = suffixItem.name
                        val rgView = suffixView.findViewById<RadioGroup>(R.id.rg)
                        for(str in suffixItem.optionList!!) {
                            val radioBtn = layoutInflater.inflate(R.layout.view_rb, null) as RadioButton
                            radioBtn.text = str
                            rgView.addView(radioBtn)
                        }
                        rgView.tag = item.key + suffixItem.key

                        //show data
                        if (!checkItemObj.isNull(item.item)) {
                            if (!checkItemObj.getJSONObject(item.item).isNull(item.key)) {
                                for (ii in 0 until rgView.childCount) {
                                    val rb = rgView[ii] as RadioButton
                                    if (rb.text.toString() == checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString(suffixItem.key)) {
                                        rb.isChecked = true
                                    }
                                }
                            }
                        }

                        itemView.addView(suffixView)
                    }
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_9fc))
                }
            }

            if (item.type == "text") {
                val itemView: View
                if (item.doubleEye) {
                    itemView = layoutInflater.inflate(R.layout.view_text_odos, null)
                    val tvNameOd = itemView.findViewById<TextView>(R.id.tv_name_od)
                    val tvNameOs = itemView.findViewById<TextView>(R.id.tv_name_os)
                    tvNameOd.text = item.name
                    tvNameOs.text = item.name

                    val edtValueOd = itemView.findViewById<EditText>(R.id.edt_value_od)
                    val edtValueOs = itemView.findViewById<EditText>(R.id.edt_value_os)
                    edtValueOd.tag = item.item+item.key+"od"
                    edtValueOs.tag = item.item+item.key+"os"

                    //show data
                    if (!checkItemObj.isNull(item.item)) {
                        edtValueOd.setText(checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString("od"))
                        edtValueOs.setText(checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString("os"))
                    }
                } else {
                    itemView = layoutInflater.inflate(R.layout.view_text_no_odos, null)
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    tvName.text = item.name

                    val edtValue = itemView.findViewById<EditText>(R.id.edt_value)
                    edtValue.tag = item.item + item.key

                    //show data
                    if (!checkItemObj.isNull(item.item)) {
                        if (!checkItemObj.getJSONObject(item.item).isNull(item.key)) {
                            edtValue.setText(checkItemObj.getJSONObject(item.item).getString(item.key))
                        }
                    }
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_9fc))
                }
            }

            if (item.type == "select") {
                val itemView: View
                if (item.doubleEye) {
                    itemView = layoutInflater.inflate(R.layout.view_select_odos, null)
                    val tvNameOd = itemView.findViewById<TextView>(R.id.tv_name_od)
                    val tvNameOs = itemView.findViewById<TextView>(R.id.tv_name_os)
                    tvNameOd.text = item.name
                    tvNameOs.text = item.name

                    val tvValueOd = itemView.findViewById<TextView>(R.id.tv_value_od)
                    val tvValueOs = itemView.findViewById<TextView>(R.id.tv_value_os)
                    tvValueOd.tag = item.item+item.key+"od"
                    tvValueOs.tag = item.item+item.key+"os"

                    //show data
                    if (!checkItemObj.isNull(item.item)) {
                        tvValueOd.text = checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString("od")
                        tvValueOs.text = checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString("os")
                    }

                    //onclick
                    onSelectHandle(tvValueOd, item.optionList)
                    onSelectHandle(tvValueOs, item.optionList)
                } else {
                    itemView = layoutInflater.inflate(R.layout.view_select_no_odos, null)
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    tvName.text = item.name

                    val tvValue = itemView.findViewById<TextView>(R.id.tv_value)
                    tvValue.tag = item.item+item.key

                    //show data
                    if (!checkItemObj.isNull(item.item)) {
                        if (!checkItemObj.getJSONObject(item.item).isNull(item.key)) {
                            tvValue.text = checkItemObj.getJSONObject(item.item).getString(item.key)
                        }
                    }

                    //onclick
                    onSelectHandle(tvValue, item.optionList)
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_9fc))
                }
            }

            if (item.type == "template") {
                val itemView = layoutInflater.inflate(R.layout.view_template_no_odos, null) as LinearLayout

                val llTemplate = itemView.findViewById<LinearLayout>(R.id.ll_template)
                val tvName = llTemplate.findViewById<TextView>(R.id.tv_name)
                tvName.text = item.name

                val str = item.template
                val separator = str.substring(0,1)
                val count = str.substring(1).split(separator).size
                for (i in 0 until count){
                    val llContent = layoutInflater.inflate(R.layout.view_template_content, null)
                    val tvSeparator = llContent.findViewById<TextView>(R.id.tv_separator)
                    tvSeparator.text = separator

                    val edtValue = llContent.findViewById<EditText>(R.id.edt_value)
                    edtValue.tag = item.item+item.key+"key$i"

                    //show data
                    if (!checkItemObj.isNull(item.item)) {
                        if (!checkItemObj.getJSONObject(item.item).isNull(item.key)) {
                            edtValue.setText(checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString("key$i"))
                        }
                    }

                    llTemplate.addView(llContent)
                }

                if (item.suffix != null) {
                    val suffixItem = item.suffix
                    if (suffixItem.type == "radio") {
                        val suffixView = layoutInflater.inflate(R.layout.view_suffix_radio_no_odos, null)
                        val tvName = suffixView.findViewById<TextView>(R.id.tv_name)
                        tvName.text = suffixItem.name
                        val rgView = suffixView.findViewById<RadioGroup>(R.id.rg)
                        for(str in suffixItem.optionList!!) {
                            val radioBtn = layoutInflater.inflate(R.layout.view_rb, null) as RadioButton
                            radioBtn.text = str
                            rgView.addView(radioBtn)
                        }
                        rgView.tag = item.key + suffixItem.key

                        //show data
                        if (!checkItemObj.isNull(item.item)) {
                            if (!checkItemObj.getJSONObject(item.item).isNull(item.key)) {
                                for (ii in 0 until rgView.childCount) {
                                    val rb = rgView[ii] as RadioButton
                                    if (rb.text.toString() == checkItemObj.getJSONObject(item.item).getJSONObject(item.key).getString(suffixItem.key)) {
                                        rb.isChecked = true
                                    }
                                }
                            }
                        }

                        itemView.addView(suffixView)
                    }
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_9fc))
                }
            }
        }

        binding.rootView.addView(childView)
    }

    private val clickKeyList = mutableSetOf<String>()
    private val clickObj = JSONObject()
    private fun showOk(comboItem: ComboItem) {
        val childView = layoutInflater.inflate(R.layout.view_ok, binding.rootView, false) as LinearLayout

        val itemList = comboItem.items
        var pos = -1
        for (item in itemList) {
            if (item.type == "radio") {
                val itemView = layoutInflater.inflate(R.layout.view_ok_radio_no_odos, null)
                childView.addView(itemView)
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                tvName.text = item.name
                val rgView = itemView.findViewById<RadioGroup>(R.id.rg)

                for (str in item.optionList!!) {
                    val radioBtn = layoutInflater.inflate(R.layout.view_ok_rb, null) as RadioButton
                    radioBtn.text = str
                    rgView.addView(radioBtn)

                    radioBtn.setOnClickListener {
                        pos = -1
                        clickKeyList.add(str)
                        var clickCount = if(!clickObj.isNull(str)) clickObj.getInt(str) else -1
                        clickCount++
                        clickObj.put(str, clickCount)
                        val okItemView = layoutInflater.inflate(R.layout.view_ok_item_odos, childView, false) as LinearLayout
                        val tvTitle = okItemView.findViewById<TextView>(R.id.tv_title)
                        tvTitle.text = str

                        val ivDelItem = okItemView.findViewById<ImageView>(R.id.iv_del_item)
                        ivDelItem.setOnClickListener {
                            childView.removeView(childView.findViewWithTag(str+clickCount))
                            clickCount--
                            clickObj.put(str, clickCount)
                        }

                        okItemView.tag = str+clickCount

                        childView.addView(okItemView)
                        val iList = item.itemList
                        if (iList != null) {
                            for(it in iList) {
                                if (str == it.item) {
                                    if (it.type == "text") {
                                        val itemView: View
                                        if (it.doubleEye) {
                                            itemView = layoutInflater.inflate(R.layout.view_text_odos, null)
                                            val tvNameOd = itemView.findViewById<TextView>(R.id.tv_name_od)
                                            val tvNameOs = itemView.findViewById<TextView>(R.id.tv_name_os)
                                            tvNameOd.text = it.name
                                            tvNameOs.text = it.name

                                            val edtValueOd = itemView.findViewById<EditText>(R.id.edt_value_od)
                                            val edtValueOs = itemView.findViewById<EditText>(R.id.edt_value_os)
                                            edtValueOd.tag = it.item + it.key + "od" + clickCount
                                            edtValueOs.tag = it.item + it.key + "os" + clickCount
                                        } else {
                                            itemView = layoutInflater.inflate(R.layout.view_text_no_odos, null)
                                            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                                            tvName.text = it.name

                                            val edtValue = itemView.findViewById<EditText>(R.id.edt_value)
                                            edtValue.tag = it.item + it.key + clickCount
                                        }

                                        okItemView.addView(itemView)
                                        pos++
                                        if (pos % 2 != 0) {
                                            itemView.setBackgroundColor(resources.getColor(R.color.color_9fc))
                                        }
                                    }
                                }
                            }
                        }
                    }


                     // handle data
                    if (!checkItemObj.isNull(item.item)){
                        val ciObj = checkItemObj.getJSONObject(item.item)
                        if (!ciObj.isNull(str)) {
                            val array = ciObj.getJSONArray(str)
                            for (index in 1..array.length()) {
                                pos = -1
                                clickKeyList.add(str)
                                var clickCount = if(!clickObj.isNull(str)) clickObj.getInt(str) else -1
                                clickCount++
                                clickObj.put(str, clickCount)
                                val okItemView = layoutInflater.inflate(R.layout.view_ok_item_odos, childView, false) as LinearLayout
                                val tvTitle = okItemView.findViewById<TextView>(R.id.tv_title)
                                tvTitle.text = str

                                val ivDelItem = okItemView.findViewById<ImageView>(R.id.iv_del_item)
                                ivDelItem.setOnClickListener {
                                    childView.removeView(childView.findViewWithTag(str+clickCount))
                                    clickCount--
                                    clickObj.put(str, clickCount)
                                }

                                okItemView.tag = str+clickCount

                                childView.addView(okItemView)
                                val iList = item.itemList
                                if (iList != null) {
                                    for(it in iList) {
                                        if (str == it.item) {
                                            if (it.type == "text") {
                                                val itemView: View
                                                if (it.doubleEye) {
                                                    itemView = layoutInflater.inflate(R.layout.view_text_odos, null)
                                                    val tvNameOd = itemView.findViewById<TextView>(R.id.tv_name_od)
                                                    val tvNameOs = itemView.findViewById<TextView>(R.id.tv_name_os)
                                                    tvNameOd.text = it.name
                                                    tvNameOs.text = it.name

                                                    val edtValueOd = itemView.findViewById<EditText>(R.id.edt_value_od)
                                                    val edtValueOs = itemView.findViewById<EditText>(R.id.edt_value_os)
                                                    edtValueOd.tag = it.item + it.key + "od" + clickCount
                                                    edtValueOs.tag = it.item + it.key + "os" + clickCount

                                                    //show data
                                                    if (!array.getJSONObject(clickCount).isNull(it.key)) {
                                                        edtValueOd.setText(array.getJSONObject(clickCount).getJSONObject(it.key).getString("od"))
                                                        edtValueOs.setText(array.getJSONObject(clickCount).getJSONObject(it.key).getString("os"))
                                                    }
                                                } else {
                                                    itemView = layoutInflater.inflate(R.layout.view_text_no_odos, null)
                                                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                                                    tvName.text = it.name

                                                    val edtValue = itemView.findViewById<EditText>(R.id.edt_value)
                                                    edtValue.tag = it.item + it.key + clickCount

                                                    //show data
                                                    if (!array.getJSONObject(clickCount).isNull(it.key)) {
                                                        edtValue.setText(array.getJSONObject(clickCount).getString(it.key))
                                                    }
                                                }

                                                okItemView.addView(itemView)
                                                pos++
                                                if (pos % 2 != 0) {
                                                    itemView.setBackgroundColor(resources.getColor(R.color.color_9fc))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.rootView.addView(childView)
    }

    private fun showSpecial(comboItem: ComboItem) {
        val childView = layoutInflater.inflate(R.layout.view_no_odos, binding.rootView, false) as LinearLayout
        val tvTitle = childView.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = comboItem.name

        val contentView= layoutInflater.inflate(R.layout.view_special_content, null) as EditText
        contentView.tag = comboItem.name

        childView.addView(contentView)

        binding.rootView.addView(childView)
    }

    private val jsonObj = JSONObject()
    private val dataObj = JSONObject()
    private fun submitData(list: MutableList<ComboItem>){
        val comboItem = list.find { it.name == comboItemStr } ?: return
        when(comboItem.category) {
            "normal" -> submitNormal(comboItem)
            "ok" -> submitOk(comboItem)
            "special" -> submitSpecial(comboItem)
        }

        if (comboItem.fileModule) {
            dataObj.put("files", "")
        }
        if (comboItem.resultModule) {
            val edtResult = binding.rootView.findViewById<EditText>(R.id.edt_result)
            dataObj.put("result", edtResult.text.toString().trim())
        }
        if (comboItem.remarkModule) {
            val edtRemark = binding.rootView.findViewById<EditText>(R.id.edt_remark)
            dataObj.put("remark", edtRemark.text.toString().trim())
        }

        jsonObj.put(comboItem.name, dataObj)

        LogUtils.e("-------$jsonObj")
    }

    private fun submitNormal(comboItem: ComboItem) {
        val itemList = comboItem.items
        for (item in itemList) {
            if (item.type == "vision" || item.type == "sph" || item.type == "cyl" || item.type == "axis"
                || item.type == "iop" || item.type == "pd" || item.type == "add" || item.type == "al"){
                if (item.doubleEye) {
                    val edtValueOd = binding.rootView.findViewWithTag(item.item+item.key+"od") as EditText
                    val edtValueOs = binding.rootView.findViewWithTag(item.item+item.key+"os") as EditText
                    val ods = JSONObject()
                    ods.put("od", edtValueOd.text.toString())
                    ods.put("os", edtValueOs.text.toString())
                    dataObj.put(item.key, ods)
                } else {
                    val edtValue = binding.rootView.findViewWithTag(item.item+item.key) as EditText
                    dataObj.put(item.key, edtValue.text.toString())
                }
            }
            if (item.type == "radio"){
                if (item.doubleEye) {
                    val rgOdValue = binding.rootView.findViewWithTag(item.item + item.key+"od") as RadioGroup
                    val rbOdValue = rgOdValue.findViewById<RadioButton>(rgOdValue.checkedRadioButtonId)
                    val rgOsValue = binding.rootView.findViewWithTag(item.item + item.key+"os") as RadioGroup
                    val rbOsValue = rgOsValue.findViewById<RadioButton>(rgOsValue.checkedRadioButtonId)
                    val ods = JSONObject()
                    ods.put("od", rbOdValue?.text?.toString() ?: "")
                    ods.put("os", rbOsValue?.text?.toString() ?: "")
                    dataObj.put(item.key, ods)
                } else {
                    val rgValue = binding.rootView.findViewWithTag(item.item + item.key) as RadioGroup
                    val rbValue = rgValue.findViewById<RadioButton>(rgValue.checkedRadioButtonId)
                    dataObj.put(item.key, rbValue?.text?.toString() ?: "")
                }
            }
            if (item.type == "text"){
                if (item.doubleEye) {
                    val edtValueOd = binding.rootView.findViewWithTag(item.item+item.key+"od") as EditText
                    val edtValueOs = binding.rootView.findViewWithTag(item.item+item.key+"os") as EditText
                    val ods = JSONObject()
                    ods.put("od", edtValueOd.text.toString())
                    ods.put("os", edtValueOs.text.toString())
                    dataObj.put(item.key, ods)
                } else {
                    val edtValue = binding.rootView.findViewWithTag(item.item+item.key) as EditText
                    dataObj.put(item.key, edtValue.text.toString())
                }
            }
            if (item.type == "select"){
                if (item.doubleEye) {
                    val tvValueOd = binding.rootView.findViewWithTag(item.item+item.key+"od") as TextView
                    val tvValueOs = binding.rootView.findViewWithTag(item.item+item.key+"os") as TextView
                    val ods = JSONObject()
                    ods.put("od", tvValueOd.text.toString())
                    ods.put("os", tvValueOs.text.toString())
                    dataObj.put(item.key, ods)
                } else {
                    val tvValue = binding.rootView.findViewWithTag(item.item+item.key) as TextView
                    dataObj.put(item.key, tvValue.text.toString())
                }
            }

            if (item.type == "template"){
                val str = item.template
                val separator = str.substring(0,1)
                val count = str.substring(1).split(separator).size
                val contentObj = JSONObject()
                for (i in 0 until count){
                    val edtValue = binding.rootView.findViewWithTag(item.item+item.key+"key$i") as EditText
                    contentObj.put("key$i", edtValue.text.toString())
                }

                if (item.suffix != null) {
                    val suffixItem = item.suffix
                    val rgValue = binding.rootView.findViewWithTag(item.key + suffixItem.key) as RadioGroup
                    val rbValue = rgValue.findViewById<RadioButton>(rgValue.checkedRadioButtonId)
                    contentObj.put(suffixItem.key, rbValue?.text?.toString() ?: "")
                }

                dataObj.put(item.key, contentObj)
            }

            if (item.type == "array"){
                val contentObj = JSONObject()
                val iList = item.itemList
                if (iList != null) {
                    for(it in iList) {
                        if (it.type == "select") {
                            val tvValue = binding.rootView.findViewWithTag(item.key+it.key) as TextView
                            contentObj.put(it.key, tvValue.text.toString())
                        }
                        if (it.type == "template"){
                            val str = it.template
                            val separator = str.substring(0,1)
                            val count = str.substring(1).split(separator).size
                            val ctObj = JSONObject()
                            for (i in 0 until count){
                                val edtValue = binding.rootView.findViewWithTag(item.key+it.key+"key$i") as EditText
                                ctObj.put("key$i", edtValue.text.toString())
                            }
                            contentObj.put(it.key, ctObj)
                        }
                        if (it.type == "text") {
                            val edtValue = binding.rootView.findViewWithTag(item.key+it.key) as EditText
                            contentObj.put(it.key, edtValue.text.toString())
                        }

                        if (it.type == "array") {
                            val ccObj = JSONObject()
                            val iList2 = it.itemList
                            if (iList2 != null) {
                                for(it2 in iList2) {
                                    if (it2.type == "template") {
                                        val str = it2.template
                                        val separator = str.substring(0,1)
                                        val count = str.substring(1).split(separator).size
                                        val ctObj = JSONObject()
                                        for (i in 0 until count){
                                            val edtValue = binding.rootView.findViewWithTag(it.key+it2.key+"key$i") as EditText
                                            ctObj.put("key$i", edtValue.text.toString())
                                        }
                                        ccObj.put(it2.key, ctObj)
                                    }
                                }
                            }
                            contentObj.put(it.key, ccObj)
                        }
                    }
                }
                if (item.suffix != null) {
                    val suffixItem = item.suffix
                    val rgValue = binding.rootView.findViewWithTag(item.key + suffixItem.key) as RadioGroup
                    val rbValue = rgValue.findViewById<RadioButton>(rgValue.checkedRadioButtonId)
                    contentObj.put(suffixItem.key, rbValue?.text?.toString() ?: "")
                }

                dataObj.put(item.key, contentObj)
            }
        }
    }

    private fun submitOk(comboItem: ComboItem) {
        val itemList = comboItem.items
        for (item in itemList) {
            if (item.type == "radio") {
                for (str in clickKeyList) {
                    val contentArray = JSONArray()
                    val count = clickObj.getInt(str)
                    for (ii in 0..count) {
                        val contentObj = JSONObject()
                        val iList = item.itemList
                        if (iList != null) {
                            for (it in iList) {
                                if (str == it.item) {
                                    if (it.type == "text") {
                                        if (it.doubleEye) {
                                            val edtValueOd = binding.rootView.findViewWithTag(it.item+it.key+"od"+ii) as EditText
                                            val edtValueOs = binding.rootView.findViewWithTag(it.item+it.key+"os"+ii) as EditText
                                            val ods = JSONObject()
                                            ods.put("od", edtValueOd.text.toString())
                                            ods.put("os", edtValueOs.text.toString())
                                            contentObj.put(it.key, ods)
                                        } else {
                                            val edtValue = binding.rootView.findViewWithTag(it.item+it.key+ii) as EditText
                                            contentObj.put(it.key, edtValue.text.toString())
                                        }
                                    }
                                }
                            }
                        }
                        contentArray.put(contentObj)
                    }
                    dataObj.put(str, contentArray)
                }
            }
        }
    }

    private fun submitSpecial(comboItem: ComboItem) {
        val edtValue = binding.rootView.findViewWithTag(comboItem.name) as EditText
        dataObj.put(comboItem.name, edtValue.text.toString().trim())
    }



    private fun onSelectHandle(tv: TextView, optionList: MutableList<String>?) {
        tv.setOnClickListener {
            MaterialDialog(requireContext()).show {
                listItems(items = optionList) { dialog, index, text ->
                    tv.text = text
                }
                lifecycleOwner(this@MainFragment)
            }
        }
    }

    private fun uploadFile(filePath: String) {
        //viewModel.uploadFile(filePath, "1")

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST ->{
                    val result = PictureSelector.obtainMultipleResult(data)
                    LogUtils.e("--------${result[0].compressPath}")
                    val filePath = result[0].compressPath
                    uploadFile(filePath)
                }
                else -> { }
            }
        }
    }
}