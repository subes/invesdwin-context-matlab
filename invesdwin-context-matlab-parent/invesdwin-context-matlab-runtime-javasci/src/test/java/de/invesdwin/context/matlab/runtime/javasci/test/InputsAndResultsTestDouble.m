disp('getDouble')
if exists('getDouble') then
	error('getDouble already defined!')
end
getDouble = putDouble
disp(class(getDouble))
disp(getDouble)
if ~isa(getDouble, 'double') then
	error('getDouble not double!')
end

disp('getDoubleVector')
if exists('getDoubleVector') then
	error('getDoubleVector already defined!')
end
getDoubleVector = putDoubleVector
disp(class(getDoubleVector))
disp(getDoubleVector)
if ~isa(getDoubleVector, 'double') then
	error('getDoubleVector not double!')
end

disp('getDoubleVectorAsList')
if exists('getDoubleVectorAsList') then
	error('getDoubleVectorAsList already defined!')
end
getDoubleVectorAsList = putDoubleVectorAsList
disp(class(getDoubleVectorAsList))
disp(getDoubleVectorAsList)
if ~isa(getDoubleVectorAsList, 'double') then
	error('getDoubleVectorAsList not double!')
end

disp('getDoubleMatrix')
if exists('getDoubleMatrix') then
	error('getDoubleMatrix already defined!')
end
getDoubleMatrix = putDoubleMatrix
disp(class(getDoubleMatrix))
disp(getDoubleMatrix)
if ~isa(getDoubleMatrix, 'double') then
	error('getDoubleMatrix not double!')
end

disp('getDoubleMatrixAsList')
if exists('getDoubleMatrixAsList') then
	error('getDoubleMatrixAsList already defined!')
end
getDoubleMatrixAsList = putDoubleMatrixAsList
disp(class(getDoubleMatrixAsList))
disp(getDoubleMatrixAsList)
if ~isa(getDoubleMatrixAsList, 'double') then
	error('getDoubleMatrixAsList not double!')
end
