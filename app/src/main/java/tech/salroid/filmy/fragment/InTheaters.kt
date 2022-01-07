package tech.salroid.filmy.fragment

import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import tech.salroid.filmy.R
import tech.salroid.filmy.activities.MainActivity
import tech.salroid.filmy.activities.MovieDetailsActivity
import tech.salroid.filmy.adapters.MainActivityAdapter
import tech.salroid.filmy.database.FilmContract
import tech.salroid.filmy.database.MovieProjection
import tech.salroid.filmy.databinding.FragmentInTheatersBinding
import tech.salroid.filmy.views.CustomToast

class InTheaters : Fragment(), LoaderManager.LoaderCallbacks<Cursor?> {

    private var mainActivityAdapter: MainActivityAdapter? = null
    private var isShowingFromDatabase = false

    private var gridLayoutManager: StaggeredGridLayoutManager? = null
    private var isInMultiWindowMode = false

    private var _binding: FragmentInTheatersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentInTheatersBinding.inflate(inflater, container, false)
        val view = binding.root
        val tabletSize = resources.getBoolean(R.bool.isTablet)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isInMultiWindowMode = activity!!.isInMultiWindowMode
        }

        when {
            tabletSize -> {
                when (activity!!.resources.configuration.orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> {
                        gridLayoutManager = StaggeredGridLayoutManager(
                            6,
                            StaggeredGridLayoutManager.VERTICAL
                        )
                        binding.recycler.layoutManager = gridLayoutManager
                    }
                    else -> {
                        if (isInMultiWindowMode) {
                            gridLayoutManager = StaggeredGridLayoutManager(
                                6,
                                StaggeredGridLayoutManager.VERTICAL
                            )
                            binding.recycler.layoutManager = gridLayoutManager
                        } else {
                            gridLayoutManager = StaggeredGridLayoutManager(
                                8,
                                StaggeredGridLayoutManager.VERTICAL
                            )
                            binding.recycler.layoutManager = gridLayoutManager
                        }
                    }
                }
            }
            else -> {
                if (activity!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    gridLayoutManager =
                        StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
                    binding.recycler.layoutManager = gridLayoutManager
                } else {
                    if (isInMultiWindowMode) {
                        gridLayoutManager =
                            StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
                        binding.recycler.layoutManager = gridLayoutManager
                    } else {
                        gridLayoutManager =
                            StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
                        binding.recycler.layoutManager = gridLayoutManager
                    }
                }
            }
        }
        mainActivityAdapter = MainActivityAdapter { itemClicked(it) }
        binding.recycler.adapter = mainActivityAdapter
        return view
    }

    override fun onResume() {
        super.onResume()
        activity?.supportLoaderManager?.initLoader(
            MovieProjection.INTHEATERS_MOVIE_LOADER,
            null,
            this
        )
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        val moviesForTheUri = FilmContract.InTheatersMoviesEntry.CONTENT_URI
        return CursorLoader(
            activity!!,
            moviesForTheUri,
            MovieProjection.MOVIE_COLUMNS,
            null,
            null,
            null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, cursor: Cursor?) {

        if (cursor != null && cursor.count > 0) {
            isShowingFromDatabase = true
            mainActivityAdapter?.swapCursor(cursor)
            binding.breathingProgress.visibility = View.GONE
        } else if (!(activity as MainActivity).fetchingFromNetwork) {
            CustomToast.show(activity, "Failed to get In Theaters movies.", true)
            (activity as MainActivity?)!!.cantProceed(-1)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        mainActivityAdapter?.swapCursor(null)
    }

    private fun itemClicked(cursor: Cursor) {
        val idIndex = cursor.getColumnIndex(FilmContract.MoviesEntry.MOVIE_ID)
        val titleIndex = cursor.getColumnIndex(FilmContract.MoviesEntry.MOVIE_TITLE)

        val intent = Intent(activity, MovieDetailsActivity::class.java)
        intent.putExtra("title", cursor.getString(titleIndex))
        intent.putExtra("activity", true)
        intent.putExtra("type", 1)
        intent.putExtra("database_applicable", true)
        intent.putExtra("network_applicable", true)
        intent.putExtra("id", cursor.getString(idIndex))
        startActivity(intent)

        activity?.overridePendingTransition(0, 0)
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)

        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutManager = if (isInMultiWindowMode) StaggeredGridLayoutManager(
                3,
                StaggeredGridLayoutManager.VERTICAL
            ) else StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
            binding.recycler.layoutManager = gridLayoutManager
            binding.recycler.adapter = mainActivityAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}