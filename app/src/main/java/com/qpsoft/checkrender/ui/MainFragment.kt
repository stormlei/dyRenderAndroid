package com.qpsoft.checkrender.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.blankj.utilcode.util.GsonUtils
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

        binding.btn.setOnClickListener {
            renderUI(comboItemList)
        }

        binding.submit.setOnClickListener {
            submitData(comboItemList)
        }
    }


    private val comboItem = "双眼视检查"
    private fun renderUI(list: MutableList<ComboItem>) {
        val comboItem = list.find { it.name == comboItem } ?: return
        when(comboItem.category) {
            "normal" -> showNormal(comboItem)
            "ok" -> showOk(comboItem)
            "special" -> showSpecial(comboItem)
        }
        if (comboItem.fileModule) {
            val childView = layoutInflater.inflate(R.layout.view_files, binding.rootView, false)
            binding.rootView.addView(childView)

            val picList = mutableListOf<String>()
            picList.add("ddddddd")
            val picAdapter = object: BaseQuickAdapter<String, BaseViewHolder>(R.layout.rv_files_item, picList) {
                override fun convert(holder: BaseViewHolder, item: String) {
                    val ivFile = holder.getView<ImageView>(R.id.iv_file)
                    Helper.loadFile(ivFile, item)
                }
            }
            val rvFiles = childView.findViewById<RecyclerView>(R.id.rv_files)
            rvFiles.layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
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
                } else {
                    itemView = layoutInflater.inflate(R.layout.view_spe_no_odos, null)
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    tvName.text = item.name

                    val edtValue = itemView.findViewById<EditText>(R.id.edt_value)
                    edtValue.tag = item.item+item.key
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
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
                        val radioBtn = RadioButton(context)
                        radioBtn.text = str
                        rgOdView.addView(radioBtn)
                    }
                    rgOdView.tag = item.item + item.key+"od"

                    val rgOsView = itemView.findViewById<RadioGroup>(R.id.rg_os)
                    for (str in item.optionList!!) {
                        val radioBtn = RadioButton(context)
                        radioBtn.text = str
                        rgOsView.addView(radioBtn)
                    }
                    rgOsView.tag = item.item + item.key+"os"
                } else {
                    itemView = layoutInflater.inflate(R.layout.view_radio_no_odos, null)
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    tvName.text = item.name
                    val rgView = itemView.findViewById<RadioGroup>(R.id.rg)
                    for (str in item.optionList!!) {
                        val radioBtn = RadioButton(context)
                        radioBtn.text = str
                        rgView.addView(radioBtn)
                    }
                    rgView.tag = item.item + item.key
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
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

                            arrayView.addView(sView)

                            onSelectHandle(tvValue, it.optionList)
                        }
                        if (it.type == "template") {
                            val llTemplate = layoutInflater.inflate(R.layout.view_array_template_no_odos, null) as LinearLayout

                            val tvName = llTemplate.findViewById<TextView>(R.id.tv_name)
                            tvName.text = it.name

                            val str = it.template
                            val separator = it.template.substring(0,1)
                            val count = str.split("$separator ").size
                            for (i in 0..count){
                                val llContent = layoutInflater.inflate(R.layout.view_template_content, null)
                                val tvSeparator = llContent.findViewById<TextView>(R.id.tv_separator)
                                tvSeparator.text = separator

                                val edtValue = llContent.findViewById<TextView>(R.id.edt_value)
                                edtValue.tag = item.key+it.key+"key$i"

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
                                        val llTemplate = layoutInflater.inflate(R.layout.view_array_template_no_odos, array2View, false) as LinearLayout

                                        val tvName = llTemplate.findViewById<TextView>(R.id.tv_name)
                                        tvName.text = it2.name

                                        val str = it2.template
                                        val separator = it2.template.substring(0,1)
                                        val count = str.split("$separator ").size
                                        for (i in 0..count){
                                            val llContent = layoutInflater.inflate(R.layout.view_template_content, null)
                                            val tvSeparator = llContent.findViewById<TextView>(R.id.tv_separator)
                                            tvSeparator.text = separator

                                            val edtValue = llContent.findViewById<TextView>(R.id.edt_value)
                                            edtValue.tag = it.key+it2.key+"key$i"

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

                            val edtValue = txtView.findViewById<TextView>(R.id.edt_value)
                            edtValue.tag = item.key+it.key

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
                            val radioBtn = RadioButton(context)
                            radioBtn.text = str
                            rgView.addView(radioBtn)
                        }
                        rgView.tag = item.key + suffixItem.key

                        itemView.addView(suffixView)
                    }
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
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
                } else {
                    itemView = layoutInflater.inflate(R.layout.view_text_no_odos, null)
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    tvName.text = item.name

                    val edtValue = itemView.findViewById<EditText>(R.id.edt_value)
                    edtValue.tag = item.item + item.key
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
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

                    //onclick
                    onSelectHandle(tvValueOd, item.optionList)
                    onSelectHandle(tvValueOs, item.optionList)
                } else {
                    itemView = layoutInflater.inflate(R.layout.view_select_no_odos, null)
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    tvName.text = item.name

                    val tvValue = itemView.findViewById<TextView>(R.id.tv_value)
                    tvValue.tag = item.item+item.key

                    //onclick
                    onSelectHandle(tvValue, item.optionList)
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
                }
            }

            if (item.type == "template") {
                val itemView = layoutInflater.inflate(R.layout.view_template_no_odos, null) as LinearLayout

                val llTemplate = itemView.findViewById<LinearLayout>(R.id.ll_template)
                val tvName = llTemplate.findViewById<TextView>(R.id.tv_name)
                tvName.text = item.name

                val str = item.template
                val separator = item.template.substring(0,1)
                val count = str.split("$separator ").size
                for (i in 0..count){
                    val llContent = layoutInflater.inflate(R.layout.view_template_content, null)
                    val tvSeparator = llContent.findViewById<TextView>(R.id.tv_separator)
                    tvSeparator.text = separator

                    val edtValue = llContent.findViewById<TextView>(R.id.edt_value)
                    edtValue.tag = item.item+item.key+"key$i"

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
                            val radioBtn = RadioButton(context)
                            radioBtn.text = str
                            rgView.addView(radioBtn)
                        }
                        rgView.tag = item.key + suffixItem.key

                        itemView.addView(suffixView)
                    }
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
                }
            }
        }

        binding.rootView.addView(childView)
    }


    private fun showOk(comboItem: ComboItem) {
        val childView = layoutInflater.inflate(R.layout.view_no_odos, binding.rootView, false) as LinearLayout

        val itemList = comboItem.items
        var pos = -1
        for (item in itemList) {
            if (item.type == "radio") {
                val itemView = layoutInflater.inflate(R.layout.view_radio_no_odos, null)
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                tvName.text = item.name
                val rgView = itemView.findViewById<RadioGroup>(R.id.rg)

                for (str in item.optionList!!) {
                    val radioBtn = RadioButton(context)
                    radioBtn.text = str
                    rgView.addView(radioBtn)

                    radioBtn.setOnClickListener {
                        val topView = layoutInflater.inflate(R.layout.view_ok_top_odos, null) as LinearLayout
                        //val tvTitle = topView.findViewById<TextView>(R.id.tv_title)
                        //tvTitle.text = str
                        childView.addView(topView)
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
                                            edtValueOd.tag = item.key + it.key + "od"
                                            edtValueOs.tag = item.key + it.key + "os"
                                        } else {
                                            itemView = layoutInflater.inflate(R.layout.view_text_no_odos, null)
                                            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                                            tvName.text = it.name

                                            val edtValue = itemView.findViewById<EditText>(R.id.edt_value)
                                            edtValue.tag = item.key + it.key
                                        }

                                        topView.addView(itemView)
                                        pos++
                                        if (pos % 2 == 0) {
                                            itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                rgView.tag = item.item + item.key

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
                }
            }
        }

        binding.rootView.addView(childView)
    }

    private fun showSpecial(comboItem: ComboItem) {

    }



    private val jsonObj = JSONObject()
    private val dataObj = JSONObject()
    private fun submitData(list: MutableList<ComboItem>){
        val comboItem = list.find { it.name == comboItem } ?: return
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
                val separator = item.template.substring(0,1)
                val count = str.split("$separator ").size
                val contentObj = JSONObject()
                for (i in 0..count){
                    val edtValue = binding.rootView.findViewWithTag(item.item+item.key+"key$i") as EditText
                    contentObj.put("value$i", edtValue.text.toString())
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
                            val count = str.split("$separator ").size
                            val ctObj = JSONObject()
                            for (i in 0..count){
                                val edtValue = binding.rootView.findViewWithTag(item.key+it.key+"key$i") as EditText
                                ctObj.put("value$i", edtValue.text.toString())
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
                                        val separator = it2.template.substring(0,1)
                                        val count = str.split("$separator ").size
                                        val ctObj = JSONObject()
                                        for (i in 0..count){
                                            val edtValue = binding.rootView.findViewWithTag(it.key+it2.key+"key$i") as EditText
                                            ctObj.put("value$i", edtValue.text.toString())
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
        jsonObj.put(comboItem.name, dataObj)

        LogUtils.e("-------$jsonObj")
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