# Description

Cell Ranger is a set of analysis pipelines that process Chromium single-cell RNA-seq output to align reads, generate feature-barcode matrices and perform clustering and gene expression analysis. Cell Ranger includes four pipelines relevant to single-cell gene expression experiments:

* [cellranger mkfastq](https://support.10xgenomics.com/single-cell-gene-expression/software/pipelines/latest/using/mkfastq) demultiplexes raw base call (BCL) files generated by Illumina sequencers into FASTQ files. It is a wrapper around Illumina's bcl2fastq, with additional useful features that are specific to 10x libraries and a simplified sample sheet format.
* [cellranger count](https://support.10xgenomics.com/single-cell-gene-expression/software/pipelines/latest/using/count) takes FASTQ files from cellranger mkfastq and performs alignment, filtering, barcode counting, and UMI counting. It uses the Chromium cellular barcodes to generate feature-barcode matrices, determine clusters, and perform gene expression analysis. The count pipeline can take input from multiple sequencing runs on the same GEM well. cellranger count also processes Feature Barcoding data alongside Gene Expression reads.
* [cellranger aggr](https://support.10xgenomics.com/single-cell-gene-expression/software/pipelines/latest/using/aggregate) aggregates outputs from multiple runs of cellranger count, normalizing those runs to the same sequencing depth and then recomputing the feature-barcode matrices and analysis on the combined data. The aggr pipeline can be used to combine data from multiple samples into an experiment-wide feature-barcode matrix and analysis.
* [cellranger reanalyze](https://support.10xgenomics.com/single-cell-gene-expression/software/pipelines/latest/using/reanalyze) takes feature-barcode matrices produced by cellranger count or cellranger aggr and reruns the dimensionality reduction, clustering, and gene expression algorithms using tunable parameter settings.

These pipelines combine Chromium-specific algorithms with the widely used RNA-seq aligner STAR. Output is delivered in standard BAM, MEX, CSV, HDF5 and HTML formats that are augmented with cellular information.

Please refer to the [10x Genomics online documentation](https://support.10xgenomics.com/single-cell-gene-expression/software/pipelines/latest/what-is-cell-ranger) for more details

# Configuration

[Cellranger 3.0.2](https://support.10xgenomics.com/single-cell-gene-expression/software/pipelines/latest/what-is-cell-ranger) is installed into `/opt/cellranger` directory

Two environment variables point to the installation location:

* `CELLRANGER_HOME` - points to the installation location: `/opt/cellranger/`
* `CELLRANGER_BIN` - points to the executable file: `/opt/cellranger/cellranger`

These variables can be used in scripts to launch the application, instead of hardcoding tool version/location.
