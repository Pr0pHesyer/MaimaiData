package com.paperpig.maimaidata.ui.rating

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.paperpig.maimaidata.databinding.FragmentRatingBinding
import com.paperpig.maimaidata.model.Rating
import com.paperpig.maimaidata.ui.BaseFragment
import com.paperpig.maimaidata.ui.maimaidxprober.LoginActivity
import com.paperpig.maimaidata.ui.maimaidxprober.ProberActivity
import com.paperpig.maimaidata.utils.SharePreferencesUtils
import com.paperpig.maimaidata.utils.getInt
import kotlin.math.floor

class RatingFragment : BaseFragment<FragmentRatingBinding>() {
    private lateinit var binding: FragmentRatingBinding
    private lateinit var resultAdapter: RatingResultAdapter

    companion object {
        @JvmStatic
        fun newInstance() =
            RatingFragment()

        const val TAG = "RatingFragment"
    }

    override fun getViewBinding(container: ViewGroup?): FragmentRatingBinding {
        binding = FragmentRatingBinding.inflate(layoutInflater, container, false)
        return binding
    }

    override fun onResume() {
        super.onResume()
        if (SharePreferencesUtils(requireContext()).getUserName().isEmpty()) {
            binding.loginLayout.visibility = View.VISIBLE
            binding.loggedLayout.visibility = View.GONE
        } else {
            binding.account.text = SharePreferencesUtils(requireContext()).getUserName()
            binding.loginLayout.visibility = View.GONE
            binding.loggedLayout.visibility = View.VISIBLE
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ratingResultRecyclerView.apply {
            resultAdapter = RatingResultAdapter()
            layoutManager = LinearLayoutManager(context)
            adapter = resultAdapter
        }

        binding.loginBtn.setOnClickListener {
            startActivity(Intent(context, LoginActivity::class.java))
        }

        binding.queryBtn.setOnClickListener {
            startActivity(Intent(context, ProberActivity::class.java))
        }

        binding.changeBtn.setOnClickListener {
            startActivity(Intent(context, LoginActivity::class.java))

        }

        binding.calculateBtn.setOnClickListener {
            hideKeyboard(view)
            onCalculate(binding.targetRatingEt.text.toString())
        }

        binding.calculateSingleRating.setOnClickListener {
            hideKeyboard(view)
            if (binding.inputSongLevel.text.toString().isNotEmpty() && binding.inputSongAchievement.text.toString()
                    .isNotEmpty())
            {
                binding.outputSingleRating.text = achievementToRating(
                    (binding.inputSongLevel.text.toString().toFloat() * 10).toInt(),
                    (binding.inputSongAchievement.text.toString().toFloat() * 10000).toInt()
                ).toString()
            }
            else{
                showToast()
            }
        }
    }

    private fun showToast() {
        val text = "请输入内容!"
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(this.context, text, duration)
        toast.show()
    }


    private fun onCalculate(targetString: String) {
        val targetRating = targetString.getInt()
        if (targetRating <= 0){
            showToast()
            return
        }
        val rating = targetRating / 40

        val minLv = getReachableLevel(rating)
        val map = mutableMapOf<Int, Int>()
        val list = mutableListOf<Rating>()

        for (i in 150 downTo minLv) {

            when (val reachableAchievement = getReachableAchievement(i, rating)) {
                800000, 900000, 940000 ->
                    map[reachableAchievement] = i
                in 970000..1010000 ->
                    map[reachableAchievement] = i
            }
        }

        map.forEach {
            list.add(
                Rating(
                    (it.value / 10f),
                    String.format("%.4f%%", it.key / 10000f),
                    achievementToRating(it.value, it.key),
                    achievementToRating(it.value, it.key) * 40
                )
            )
        }

        resultAdapter.setData(list)

    }
}


private fun getReachableLevel(rating: Int): Int {
    for (i in 10..150) {
        if (rating < achievementToRating(i, 1005000)) {
            return i
        }
    }
    return 0
}

private fun getReachableAchievement(level: Int, rating: Int): Int {
    var maxAchi = 1010000
    var minAchi = 0
    var tempAchi: Int


    if (achievementToRating(level, 1005000) < rating)
        return 1010001
    for (n in 0..20) {
        if (maxAchi - minAchi >= 2) {
            tempAchi = floor((maxAchi.toDouble() + minAchi) / 2).toInt()

            if (achievementToRating(level, tempAchi) < rating)
                minAchi = tempAchi
            else maxAchi = tempAchi
        }
    }
    return maxAchi
}

private fun achievementToRating(level: Int, achi: Int): Int {
    val i = when {
        achi >= 1005000 -> {
            14.0
        }
        achi >= 1000000 -> {
            13.5
        }
        achi >= 995000 -> {
            13.2
        }
        achi >= 990000 -> {
            13.0
        }
        achi >= 980000 -> {
            12.7
        }
        achi >= 970000 -> {
            12.5
        }
        achi >= 940000 -> {
            10.5
        }
        achi >= 900000 -> {
            9.5
        }
        achi >= 800000 -> {
            8.5
        }
        achi >= 750000 -> {
            7.5
        }
        achi >= 700000 -> {
            7.0
        }
        achi >= 600000 -> {
            6.0
        }
        achi >= 500000 -> {
            5.0
        }
        achi >= 400000 -> {
            4.0
        }
        achi >= 300000 -> {
            3.0
        }
        achi >= 200000 -> {
            2.0
        }
        achi >= 100000 -> {
            1.0
        }

        else -> 0.0
    }


    val temp = achi.coerceAtMost(1005000) * level * i
    return (temp / 10000000).toInt()

}