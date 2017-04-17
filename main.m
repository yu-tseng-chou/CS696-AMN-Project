% Clean up
clc;clear;clf;close all;

% Loading the images
filelist = [dir('images/*.jpg');dir('images/*.png')];
n = length(filelist);
numSubplotRows = 3;
numSubplotCols = 1;

for i=1:n
    autoSubplotter = MakeAutoSubplot(numSubplotRows, numSubplotCols);
    
    %% Load the original image
    imname = filelist(i).name;
	im = imread(['images/' imname]);
   
    figure
    autoSubplotter();
    imshow(im);
    title(sprintf('Original file %s', strrep(imname, '_', '\_')));
    
    %% Filter image with a threshold so only black and white left
%     threshold = ComputeThreshold(counts);
    threshold = 50;
    T = im;
    T(T > threshold) = 255;
    T(T <= threshold) = 0;
    autoSubplotter();
    imshow(T);
    title(sprintf('Filtered image with threshold = %d', threshold));
    
    %% Remove noise with Median Filter
    autoSubplotter();
    filterWidth = 3;
    singleRowDecomp = MedianDecomposition(T,filterWidth);
    % Vertically stretch decomposed row to make viewing easier
    stretchedDecmop = repmat(singleRowDecomp,size(T,1),1);
    imshow(stretchedDecmop);
    title(sprintf('After median decomposition of width = %d', filterWidth));
    
    %% Extract song length information
    height = size(T,1);
    song_lengths = SongLengthsExtraction(singleRowDecomp);
    g = sprintf('%d, ', song_lengths);
    g = g(1:size(g,2)-2); % Trim off trailing comma
    text(0, height+35, g);
end