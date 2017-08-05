{-# LANGUAGE OverloadedStrings #-}
{-# LANGUAGE TupleSections #-}
{-# LANGUAGE TemplateHaskell #-}
module Databricks where

import Data.Default
import Data.Text (Text, unpack)
import qualified Data.List as L
import qualified Data.UUID as UUID
import qualified Data.Text as T
import Data.Maybe (fromJust)
import Data.Aeson
import Data.Monoid ((<>))
import Data.Traversable (mapAccumL)
import qualified Data.ByteString.Lazy as B
import qualified Data.HashMap.Lazy as H
import qualified Text.Pandoc.Builder as P
import Control.Lens hiding ((.=))
import Control.Monad (when, unless)
import Utils
import qualified Notebook as N
import Codec.Archive.Zip as Zip
import qualified Data.Char as C (toLower)
import qualified Text.Pandoc.Readers.HTML as P
import qualified Data.Vector as V

-- jsonType :: Value -> String
-- jsonType (Object _) = "Object"
-- jsonType (Array _) = "Array"
-- jsonType (String _) = "String"
-- jsonType (Number _) = "Number"
-- jsonType (Bool _) = "Bool"
-- jsonType Null = "Null"

-- jsonKeys :: Value -> [Text]
-- jsonKeys (Object o) = H.keys o
-- jsonKeys _ = []

data DBNotebook = DBN { _dbnCommands     :: [DBCommand]
                      , _dbnDashboards   :: Maybe [Value]
                      , _dbnIPythonMeta  :: Maybe Value
                      , _dbnInputWidgets :: Maybe Value
                      , _dbnName         :: Text
                      , _dbnGuid         :: UUID.UUID
                      , _dbnVersion      :: Maybe Value -- some sort of enum
                      , _dbnLanguage     :: Text
                      , _dbnGlobalVars   :: Maybe Value
                      , _dbnOrigID       :: Maybe Value } -- Integer?
  deriving Show

instance Default DBNotebook where
  def = DBN [] Nothing Nothing Nothing "" def Nothing "md" Nothing Nothing

data DBCommand = DBC { _dbcCustomPlotOptions :: Maybe Value
                     , _dbcErrorSummary      :: Maybe Value
                     , _dbcHeight            :: Maybe Value
                     , _dbcDiffDeletes       :: Maybe Value
                     , _dbcCommandTitle      :: Maybe Text
                     , _dbcState             :: Maybe Value
                     , _dbcCommand           :: Text
                     , _dbcResults           :: Maybe DBResult
                     , _dbcCommandVersion    :: Maybe Value
                     , _dbcXColumns          :: Maybe Value
                     , _dbcStartTime         :: Maybe Value
                     , _dbcIPythonMetadata   :: Maybe Value
                     , _dbcError             :: Maybe Value
                     , _dbcPivotAggregation  :: Maybe Value
                     , _dbcWidth             :: Maybe Value
                     , _dbcNuid              :: Maybe Text
                     , _dbcPivotColumns      :: Maybe Value
                     , _dbcInputWidgets      :: Maybe Value
                     , _dbcSubtype           :: Maybe Value
                     , _dbcYColumns          :: Maybe Value
                     , _dbcShowCommandTitle  :: Maybe Value
                     , _dbcGuid              :: UUID.UUID
                     , _dbcCommandType       :: Maybe Value
                     , _dbcCollapsed         :: Maybe Value
                     , _dbcVersion           :: Maybe Value
                     , _dbcLatestUser        :: Maybe Value
                     , _dbcBindings          :: Maybe Value
                     , _dbcHideCommandCode   :: Maybe Bool
                     , _dbcDisplayType       :: Maybe Value
                     , _dbcGlobalVars        :: Maybe Value
                     , _dbcCommentThread     :: Maybe Value
                     , _dbcWorkflows         :: Maybe Value
                     , _dbcParentHierarchy   :: Maybe Value
                     , _dbcHideCommandResult :: Maybe Bool
                     , _dbcFinishTime        :: Maybe Value
                     , _dbcCommentsVisible   :: Maybe Value
                     , _dbcOrigId            :: Maybe Value
                     , _dbcSubmitTime        :: Maybe Value
                     , _dbcDiffInserts       :: Maybe Value
                     , _dbcPosition          :: Double }
  deriving Show

data DBResult = DBR { _dbrAddedWidgets   :: Maybe Value
                    , _dbrData           :: Maybe Value
                    , _dbrArguments      :: Maybe Value
                    , _dbrRemovedWidgets :: Maybe Value
                    , _dbrType           :: Maybe Value }
  deriving Show

makeLenses ''DBNotebook
makeLenses ''DBCommand
makeLenses ''DBResult

instance Default DBResult where
  def = DBR Nothing Nothing Nothing Nothing Nothing

instance ToJSON DBResult where
  toJSON dbr = objectMaybe [ "addedWidgets" .=? (dbr^.dbrAddedWidgets)
                           , "data" .=? (dbr^.dbrData)
                           , "arguments" .=? (dbr^.dbrArguments)
                           , "removedWidgets" .=? (dbr^.dbrRemovedWidgets)
                           , "type" .=? (dbr^.dbrType) ]

  toEncoding dbr = pairs ( "addedWidgets" .=? (dbr^.dbrAddedWidgets)
                           <> "data" .=? (dbr^.dbrData)
                           <> "arguments" .=? (dbr^.dbrArguments)
                           <> "removedWidgets" .=? (dbr^.dbrRemovedWidgets)
                           <> "type" .=? (dbr^.dbrType) )

instance FromJSON DBResult where
  parseJSON = withObject "DBResult" $ \v -> DBR
    <$> v .:? "addedWidgets"
    <*> v .:? "data"
    <*> v .:? "arguments"
    <*> v .:? "removedWidgets"
    <*> v .:? "type"

instance Default DBCommand where
  def = DBC Nothing Nothing Nothing Nothing Nothing Nothing "" Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing def Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing Nothing 0

instance ToJSON DBCommand where
  toJSON dbc = objectMaybe [ "customPlotOptions" .=? (dbc^.dbcCustomPlotOptions)
                           , "errorSummary" .=? (dbc^.dbcErrorSummary)
                           , "height" .=? (dbc^.dbcHeight)
                           , "diffDeletes" .=? (dbc^.dbcDiffDeletes)
                           , "commandTitle" .=? (dbc^.dbcCommandTitle)
                           , "state" .=? (dbc^.dbcState)
                           , "command" .= (dbc^.dbcCommand)
                           , "results" .=? (dbc^.dbcResults)
                           , "commandVersion" .=? (dbc^.dbcCommandVersion)
                           , "xColumns" .=? (dbc^.dbcXColumns)
                           , "startTime" .=? (dbc^.dbcStartTime)
                           , "iPythonMetadata" .=? (dbc^.dbcIPythonMetadata)
                           , "error" .=? (dbc^.dbcError)
                           , "pivotAggregation" .=? (dbc^.dbcPivotAggregation)
                           , "width" .=? (dbc^.dbcWidth)
                           , "nuid" .=? (dbc^.dbcNuid)
                           , "pivotColumns" .=? (dbc^.dbcPivotColumns)
                           , "inputWidgets" .=? (dbc^.dbcInputWidgets)
                           , "subtype" .=? (dbc^.dbcSubtype)
                           , "yColumns" .=? (dbc^.dbcYColumns)
                           , "showCommandTitle" .=? (dbc^.dbcShowCommandTitle)
                           , "guid" .= (dbc^.dbcGuid)
                           , "commandType" .=? (dbc^.dbcCommandType)
                           , "collapsed" .=? (dbc^.dbcCollapsed)
                           , "version" .=? (dbc^.dbcVersion)
                           , "latestUser" .=? (dbc^.dbcLatestUser)
                           , "bindings" .=? (dbc^.dbcBindings)
                           , "hideCommandCode" .=? (dbc^.dbcHideCommandCode)
                           , "displayType" .=? (dbc^.dbcDisplayType)
                           , "globalVars" .=? (dbc^.dbcGlobalVars)
                           , "commentThread" .=? (dbc^.dbcCommentThread)
                           , "workflows" .=? (dbc^.dbcWorkflows)
                           , "parentHierarchy" .=? (dbc^.dbcParentHierarchy)
                           , "hideCommandResult" .=? (dbc^.dbcHideCommandResult)
                           , "finishTime" .=? (dbc^.dbcFinishTime)
                           , "commentsVisible" .=? (dbc^.dbcCommentsVisible)
                           , "origId" .=? (dbc^.dbcOrigId)
                           , "submitTime" .=? (dbc^.dbcSubmitTime)
                           , "diffInserts" .=? (dbc^.dbcDiffInserts)
                           , "position" .= (dbc^.dbcPosition) ]

  toEncoding dbc = pairs ( "customPlotOptions" .=? (dbc^.dbcCustomPlotOptions)
                           <> "errorSummary" .=? (dbc^.dbcErrorSummary)
                           <> "height" .=? (dbc^.dbcHeight)
                           <> "diffDeletes" .=? (dbc^.dbcDiffDeletes)
                           <> "commandTitle" .=? (dbc^.dbcCommandTitle)
                           <> "state" .=? (dbc^.dbcState)
                           <> "command" .= (dbc^.dbcCommand)
                           <> "results" .=? (dbc^.dbcResults)
                           <> "commandVersion" .=? (dbc^.dbcCommandVersion)
                           <> "xColumns" .=? (dbc^.dbcXColumns)
                           <> "startTime" .=? (dbc^.dbcStartTime)
                           <> "iPythonMetadata" .=? (dbc^.dbcIPythonMetadata)
                           <> "error" .=? (dbc^.dbcError)
                           <> "pivotAggregation" .=? (dbc^.dbcPivotAggregation)
                           <> "width" .=? (dbc^.dbcWidth)
                           <> "nuid" .=? (dbc^.dbcNuid)
                           <> "pivotColumns" .=? (dbc^.dbcPivotColumns)
                           <> "inputWidgets" .=? (dbc^.dbcInputWidgets)
                           <> "subtype" .=? (dbc^.dbcSubtype)
                           <> "yColumns" .=? (dbc^.dbcYColumns)
                           <> "showCommandTitle" .=? (dbc^.dbcShowCommandTitle)
                           <> "guid" .= (dbc^.dbcGuid)
                           <> "commandType" .=? (dbc^.dbcCommandType)
                           <> "collapsed" .=? (dbc^.dbcCollapsed)
                           <> "version" .=? (dbc^.dbcVersion)
                           <> "latestUser" .=? (dbc^.dbcLatestUser)
                           <> "bindings" .=? (dbc^.dbcBindings)
                           <> "hideCommandCode" .=? (dbc^.dbcHideCommandCode)
                           <> "displayType" .=? (dbc^.dbcDisplayType)
                           <> "globalVars" .=? (dbc^.dbcGlobalVars)
                           <> "commentThread" .=? (dbc^.dbcCommentThread)
                           <> "workflows" .=? (dbc^.dbcWorkflows)
                           <> "parentHierarchy" .=? (dbc^.dbcParentHierarchy)
                           <> "hideCommandResult" .=? (dbc^.dbcHideCommandResult)
                           <> "finishTime" .=? (dbc^.dbcFinishTime)
                           <> "commentsVisible" .=? (dbc^.dbcCommentsVisible)
                           <> "origId" .=? (dbc^.dbcOrigId)
                           <> "submitTime" .=? (dbc^.dbcSubmitTime)
                           <> "diffInserts" .=? (dbc^.dbcDiffInserts)
                           <> "position" .= (dbc^.dbcPosition) )

instance FromJSON DBCommand where
  parseJSON = withObject "DBCommand" $ \v -> DBC
    <$> v .: "customPlotOptions"
    <*> v .: "errorSummary"
    <*> v .: "height"
    <*> v .: "diffDeletes"
    <*> v .: "commandTitle"
    <*> v .: "state"
    <*> v .: "command"
    <*> v .: "results"
    <*> v .: "commandVersion"
    <*> v .: "xColumns"
    <*> v .: "startTime"
    <*> v .: "iPythonMetadata"
    <*> v .: "error"
    <*> v .: "pivotAggregation"
    <*> v .: "width"
    <*> v .: "nuid"
    <*> v .: "pivotColumns"
    <*> v .: "inputWidgets"
    <*> v .: "subtype"
    <*> v .: "yColumns"
    <*> v .: "showCommandTitle"
    <*> v .: "guid"
    <*> v .: "commandType"
    <*> v .: "collapsed"
    <*> v .: "version"
    <*> v .: "latestUser"
    <*> v .: "bindings"
    <*> v .: "hideCommandCode"
    <*> v .: "displayType"
    <*> v .: "globalVars"
    <*> v .: "commentThread"
    <*> v .: "workflows"
    <*> v .: "parentHierarchy"
    <*> v .: "hideCommandResult"
    <*> v .: "finishTime"
    <*> v .: "commentsVisible"
    <*> v .: "origId"
    <*> v .: "submitTime"
    <*> v .: "diffInserts"
    <*> v .: "position"

instance ToJSON DBNotebook where
  toJSON dbn = object [ "commands" .= (dbn^.dbnCommands)
                      , "dashboards" .= (dbn^.dbnDashboards)
                      , "iPythonMetadata" .= (dbn^.dbnIPythonMeta)
                      , "inputWidgets" .= (dbn^.dbnInputWidgets)
                      , "name" .= (dbn^.dbnName)
                      , "guid" .= (dbn^.dbnGuid)
                      , "version" .= (dbn^.dbnVersion)
                      , "language" .= (dbn^.dbnLanguage)
                      , "globalVars" .= (dbn^.dbnGlobalVars)
                      , "origId" .= (dbn^.dbnOrigID) ]

  toEncoding dbn = pairs ( "commands" .= (dbn^.dbnCommands)
                         <> "dashboards" .= (dbn^.dbnDashboards)
                         <> "iPythonMetadata" .= (dbn^.dbnIPythonMeta)
                         <> "inputWidgets" .= (dbn^.dbnInputWidgets)
                         <> "name" .= (dbn^.dbnName)
                         <> "guid" .= (dbn^.dbnGuid)
                         <> "version" .= (dbn^.dbnVersion)
                         <> "language" .= (dbn^.dbnLanguage)
                         <> "globalVars" .= (dbn^.dbnGlobalVars)
                         <> "origId" .= (dbn^.dbnOrigID) )

instance FromJSON DBNotebook where
  parseJSON = withObject "DBNotebook" $ \v -> DBN
    <$> v .: "commands"
    <*> v .: "dashboards"
    <*> v .: "iPythonMetadata"
    <*> v .: "inputWidgets"
    <*> v .: "name"
    <*> v .: "guid"
    <*> v .: "version"
    <*> v .: "language"
    <*> v .: "globalVars"
    <*> v .: "origId"

fromByteString :: B.ByteString -> Either String DBNotebook
fromByteString = eitherDecode

toByteString :: DBNotebook -> B.ByteString
toByteString = encode

toNotebook :: DBNotebook -> N.Notebook
toNotebook db = N.N (db^.dbnName) (toCommands (db^.dbnCommands))
  where toCommands = map toCommand
        toCommand :: DBCommand -> N.Command
        toCommand dbc =
          let (langTag, rawCommand) = splitLangTag (dbc^.dbcCommand)
              result = do r <- dbc^.dbcResults
                          t <- r^.dbrType
                          if (t `L.elem` [String "html", String "htmlSandbox"])
                            then parseHTML r
                            else if (t == String "table")
                                 then parseTable r
                                 else Nothing
              parseHTML r = do
                d' <- r^.dbrData
                case d' of
                  String t ->
                    case P.readHtml def (T.unpack $ t) of
                      Right (P.Pandoc _ bs) -> return (N.RSuccess (blocks bs))
                      _ -> Nothing
                  _ -> Nothing
              parseTable r = do
                d' <- r^.dbrData
                case d' of
                  Array arr -> do
                    let trunc' (Array row) = mapM trunc'' (V.toList (V.take 12 row))
                        trunc' _ = Nothing
                        trunc'' (Number cell) = Just (P.plain (P.str (show cell)))
                        trunc'' (String cell) = Just (P.plain (P.str (T.unpack cell)))
                        trunc'' _ = Nothing
                        wasRowTrunc = V.length arr > 30
                        wasColTrunc = V.any (\(Array row) -> V.length row > 12) arr
                    trunc <- mapM trunc' (V.toList (V.take 30 arr))
                    case trunc of
                      [] -> Nothing
                      (x:xs) -> return (N.RSuccess (P.simpleTable x xs <> (if wasRowTrunc then P.para (P.str "Truncated to 30 rows") else mempty) <> (if wasColTrunc then P.para (P.str "Truncated to 12 cols") else mempty)))
                  _ -> Nothing
          in case langTag of
               Nothing   -> N.C (db^.dbnLanguage) rawCommand result
               Just lang -> N.C lang rawCommand result
        splitLangTag unparsedCommand =
          if maybe False (== '%') (unparsedCommand `safeIndex` 0)
          then let (x:xs) = T.lines unparsedCommand
               in (Just (T.stripEnd . T.tail $ x), T.unlines xs)
          else (Nothing, unparsedCommand)

fromNotebook :: N.Notebook -> DBNotebook
fromNotebook nb = defWith [ dbnName .~ (nb^.N.nName)
                          , dbnCommands .~ map toNBCommand (nb^.N.nCommands) ]
  where toNBCommand nc =
          defWith [ dbcCommand .~ addLang (nc^.N.cLanguage) (nc^.N.cCommand) ]
        addLang l c = T.unlines [ T.cons '%' l, c ]

fromByteStringArchive :: B.ByteString -> Either String [(FilePath, DBNotebook)]
fromByteStringArchive x = mapM (\f -> (f,) <$> getNotebook f) jsonPaths
  where archive = Zip.toArchive x
        jsonPaths = filter isJSON (Zip.filesInArchive archive)
        isJSON f = let f' = map C.toLower f
                   in any (`L.isSuffixOf` f') [".scala", ".py", ".r", ".sql"]
        getNotebook f = fromByteString $
                        Zip.fromEntry $
                        fromJust $
                        Zip.findEntryByPath f archive

-- main :: IO ()
-- main = do
--   --[p] <- getArgs
--   -- let p = "data/20160826_KDL_Intro2BDA_HumSocSci/00_workshop_Overview.scala"
--   -- let p = "data/20160826_KDL_Intro2BDA_HumSocSci/01_introduction/002_loginToDatabricksCE.scala"
--   let p = "data/scalable-data-science/week5/10_LinearRegressionIntro/018_LinRegIntro.scala"
--   f <- B.readFile p
--   let dbn = either error id (eitherDecode f :: Either String DBNotebook)
--   -- print (jsonType $ dbcCommand (dbnCommands dbn !! 0))
--   -- print (jsonKeys $ dbcCommand (dbnCommands dbn !! 0))
--   -- print (dbnCommands dbn !! 0)
--   print (jsonType $ dbnOrigID dbn)
--   print (jsonKeys $ dbnOrigID dbn)
--   print (dbnOrigID dbn)
--   return ()
