function medianVector = MedianDecomposition(img,filterWidth)
% Get a vector w/ the median value at each step of sweeping a median filter
% left to right across the image.
%   img - 2d matrix with image to decompose
%   filterWidth - width of the median filter

    imgWidth  = size(img,2);
    imgHeight = size(img,1);

    % pre-allocate vector to be returned
    nSteps = imgWidth - filterWidth + 1;
    medianVector = zeros(1,nSteps);
    
    % sweep median filter from left to right
    % filter height = image height
    tmp = zeros(1, imgHeight * filterWidth);
    for i=1:nSteps
        tmp = reshape(img(:,i:i+filterWidth-1), 1, size(tmp,2));
        medianVector(i) = median(tmp); 
    end
end